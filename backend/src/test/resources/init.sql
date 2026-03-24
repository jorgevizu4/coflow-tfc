-- =====================================================
-- INIT.SQL - Sistema de Gestión de Tareas Multi-Tenant
-- PostgreSQL 15+ | Spring Boot 3.x Compatible
-- =====================================================

-- 1. Limpieza inicial (Cuidado: Esto borra datos existentes)
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

-- 2. Creación de Tipos ENUM (Para consistencia en el Flujo)
CREATE TYPE estado_tarea_enum AS ENUM (
    'PENDIENTE', 
    'ASIGNADA', 
    'EN_PROCESO', 
    'BLOQUEADA',     -- Para el flujo de "Bloqueada -> 24h -> Notificar"
    'EN_REVISION',   -- Para el flujo "¿Requiere revisión?"
    'APROBADA', 
    'RECHAZADA',
    'COMPLETADA'
);

CREATE TYPE prioridad_enum AS ENUM ('BAJA', 'MEDIA', 'ALTA', 'URGENTE');

-- NUEVO: Enum para roles de usuario (Control de acceso por rol)
CREATE TYPE rol_usuario_enum AS ENUM ('ADMIN', 'LIDER', 'REVISOR', 'USER');

-- 3. Tabla EMPRESA (Nueva: Para soportar "Seleccionar empresa" del flujo)
CREATE TABLE empresa (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT NOW()
);

-- 4. Tabla USUARIO (Adaptada con roles y estado activo)
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    empresa_id INT REFERENCES empresa(id) ON DELETE CASCADE, -- Multi-tenancy
    nombre_completo VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- BCrypt hash
    rol rol_usuario_enum DEFAULT 'USER', -- NUEVO: Rol para JWT claims
    activo BOOLEAN DEFAULT TRUE,         -- NUEVO: Para desactivar cuentas
    estado_actual VARCHAR(50),
    configuracion_ia JSONB, -- Campo original del diagrama
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    ultimo_login TIMESTAMP               -- NUEVO: Seguimiento de sesiones
);

-- 5. Tabla EQUIPO
CREATE TABLE equipo (
    id SERIAL PRIMARY KEY,
    empresa_id INT REFERENCES empresa(id) ON DELETE CASCADE,
    lider_id INT REFERENCES usuario(id),
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT
);

-- Tabla intermedia MIEMBROS_EQUIPO (Relación N:M del diagrama)
CREATE TABLE miembros_equipo (
    equipo_id INT REFERENCES equipo(id) ON DELETE CASCADE,
    usuario_id INT REFERENCES usuario(id) ON DELETE CASCADE,
    rol VARCHAR(50) DEFAULT 'MIEMBRO', -- Enum: ADMIN, MIEMBRO
    fecha_union TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (equipo_id, usuario_id)
);

-- 6. Tabla PROYECTO
CREATE TABLE proyecto (
    id SERIAL PRIMARY KEY,
    empresa_id INT REFERENCES empresa(id) ON DELETE CASCADE,
    lider_id INT REFERENCES usuario(id),
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    fecha_inicio TIMESTAMP DEFAULT NOW(),
    fecha_fin_estimada TIMESTAMP
);

-- 7. Tabla TAREA (El núcleo del diagrama de flujo)
CREATE TABLE tarea (
    id SERIAL PRIMARY KEY,
    empresa_id INT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE, -- NUEVO: Multi-tenancy directo
    proyecto_id INT NOT NULL REFERENCES proyecto(id) ON DELETE CASCADE, -- Siempre pertenece a proyecto
    usuario_asignado_id INT REFERENCES usuario(id), -- Responsable
    creador_id INT REFERENCES usuario(id),          -- Quien hizo "Generar tarea"
    
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    
    -- Control de Tiempos
    tiempo_estimado INT, -- En minutos
    tiempo_real INT DEFAULT 0,
    fecha_limite TIMESTAMP,
    fecha_bloqueo TIMESTAMP, -- Para calcular las "24h" del flujo
    
    -- Control de Flujo y Estados
    estado estado_tarea_enum DEFAULT 'PENDIENTE',
    prioridad prioridad_enum DEFAULT 'MEDIA',
    requiere_revision BOOLEAN DEFAULT FALSE, -- Crucial para el rombo "¿Requiere revisión?"
    eliminada BOOLEAN NOT NULL DEFAULT FALSE,
    eliminada_at TIMESTAMP,
    eliminada_por_usuario_id BIGINT REFERENCES usuario(id),
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 8. Tabla ENTREGABLE (Para soportar "Entregar docs")
CREATE TABLE entregable (
    id SERIAL PRIMARY KEY,
    empresa_id INT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE, -- NUEVO: Multi-tenancy
    tarea_id INT REFERENCES tarea(id) ON DELETE CASCADE,
    usuario_id INT REFERENCES usuario(id), -- Quien subió el doc
    url_archivo TEXT NOT NULL,             -- Link a S3/Azure/MinIO o path local
    nombre_archivo VARCHAR(255),
    tipo_contenido VARCHAR(100),           -- NUEVO: MIME type
    tamano_bytes BIGINT,                   -- NUEVO: Tamaño del archivo
    comentarios TEXT,
    version INT DEFAULT 1,
    fecha_subida TIMESTAMP DEFAULT NOW()
);

-- 9. Tablas Auxiliares del Diagrama Original (Subtareas, Etiquetas, Comentarios)

CREATE TABLE subtarea (
    id SERIAL PRIMARY KEY,
    empresa_id INT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE, -- NUEVO: Multi-tenancy
    tarea_padre_id INT REFERENCES tarea(id) ON DELETE CASCADE,
    titulo VARCHAR(200),
    completada BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE etiqueta (
    id SERIAL PRIMARY KEY,
    empresa_id INT REFERENCES empresa(id) ON DELETE CASCADE,
    nombre VARCHAR(50),
    color_hex VARCHAR(7)
);

CREATE TABLE tareas_etiquetas (
    tarea_id INT REFERENCES tarea(id) ON DELETE CASCADE,
    etiqueta_id INT REFERENCES etiqueta(id) ON DELETE CASCADE,
    PRIMARY KEY (tarea_id, etiqueta_id)
);

CREATE TABLE comentario (
    id SERIAL PRIMARY KEY,
    empresa_id INT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE, -- NUEVO: Multi-tenancy
    tarea_id INT REFERENCES tarea(id) ON DELETE CASCADE,
    usuario_id INT REFERENCES usuario(id),
    contenido TEXT NOT NULL,
    tipo VARCHAR(50) DEFAULT 'COMENTARIO', -- NUEVO: 'COMENTARIO', 'RECHAZO', 'APROBACION'
    fecha_creacion TIMESTAMP DEFAULT NOW()
);

-- 10. Tabla REGISTRO_ACTIVIDAD (Reestructurada para AOP Auditing)
CREATE TABLE registro_actividad (
    id BIGSERIAL PRIMARY KEY,
    empresa_id INT REFERENCES empresa(id) ON DELETE CASCADE, -- NUEVO: Multi-tenancy
    usuario_id INT REFERENCES usuario(id),
    
    -- Campos AOP Auditing
    entidad VARCHAR(100) NOT NULL,         -- 'TAREA', 'PROYECTO', 'USUARIO', etc.
    entidad_id BIGINT,                     -- ID de la entidad afectada
    accion VARCHAR(50) NOT NULL,           -- 'CREATE', 'UPDATE', 'DELETE', 'ESTADO_CHANGE'
    metodo VARCHAR(200),                   -- Firma del método interceptado
    
    -- Tracking de cambios
    valores_anteriores JSONB,              -- Estado anterior (para UPDATE/DELETE)
    valores_nuevos JSONB,                  -- Estado nuevo (para CREATE/UPDATE)
    
    -- Contexto de la petición
    ip_address VARCHAR(45),                -- IPv4 o IPv6
    user_agent TEXT,                       -- Navegador/cliente
    
    -- Campos legacy (compatibilidad)
    app_detectada VARCHAR(100),
    categoria VARCHAR(100),
    
    -- Timestamps y retención
    created_at TIMESTAMP DEFAULT NOW(),
    fecha_expiracion TIMESTAMP DEFAULT (NOW() + INTERVAL '90 days') -- NUEVO: Política de retención
);

-- Índice para job de limpieza de auditoría
CREATE INDEX idx_registro_expiracion ON registro_actividad(fecha_expiracion);
CREATE INDEX idx_registro_empresa ON registro_actividad(empresa_id);
CREATE INDEX idx_registro_entidad ON registro_actividad(entidad, entidad_id);

CREATE TABLE accion_agente (
    id SERIAL PRIMARY KEY,
    empresa_id INT REFERENCES empresa(id) ON DELETE CASCADE, -- NUEVO: Multi-tenancy
    usuario_id INT REFERENCES usuario(id),
    accion_tipo VARCHAR(100),
    detalle TEXT,
    fecha TIMESTAMP DEFAULT NOW()
);

CREATE TABLE evento_externo (
    id SERIAL PRIMARY KEY,
    empresa_id INT REFERENCES empresa(id) ON DELETE CASCADE, -- NUEVO: Multi-tenancy
    usuario_id INT REFERENCES usuario(id),
    origen VARCHAR(50), -- Gmail, Slack, etc.
    inicio TIMESTAMP
);

-- 11. Tabla NOTIFICACION (Para el flujo "Notificar" tras bloqueo)
CREATE TABLE notificacion (
    id SERIAL PRIMARY KEY,
    empresa_id INT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE, -- NUEVO: Multi-tenancy
    usuario_id INT REFERENCES usuario(id),
    tarea_id INT REFERENCES tarea(id) ON DELETE CASCADE, -- NUEVO: Referencia a tarea
    mensaje TEXT NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    tipo VARCHAR(50), -- 'ALERTA_BLOQUEO', 'ASIGNACION', 'REVISION', 'APROBACION', 'RECHAZO'
    fecha_creacion TIMESTAMP DEFAULT NOW()
);

-- INDICES (Para optimizar las consultas del Worker/Scheduler)
CREATE INDEX idx_tarea_estado ON tarea(estado);
CREATE INDEX idx_tarea_empresa ON tarea(empresa_id);
CREATE INDEX idx_tarea_bloqueo ON tarea(fecha_bloqueo) WHERE estado = 'BLOQUEADA';
CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_usuario_empresa ON usuario(empresa_id);
CREATE INDEX idx_notificacion_usuario ON notificacion(usuario_id, leida);
CREATE INDEX idx_entregable_tarea ON entregable(tarea_id);
CREATE INDEX idx_comentario_tarea ON comentario(tarea_id);

-- FUNCIÓN DE UTILIDAD: Actualizar timestamp automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tarea_modtime 
BEFORE UPDATE ON tarea 
FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

-- =====================================================
-- DATOS DE PRUEBA (Seed Data para desarrollo)
-- =====================================================

-- Empresa de ejemplo
INSERT INTO empresa (nombre) VALUES 
    ('TechCorp S.A.'),
    ('Innovate Labs');

-- Usuarios de prueba (password: "password123" hasheado con BCrypt)
INSERT INTO usuario (empresa_id, nombre_completo, email, password_hash, rol, activo) VALUES
    (1, 'Admin TechCorp', 'admin@techcorp.com', '$2a$10$N9qo8uLOickgx2ZMRZoHK.Z2PtJfDX7T5YdMh5VT5cVb5dLR5z5K2', 'ADMIN', true),
    (1, 'María García (Líder)', 'maria.garcia@techcorp.com', '$2a$10$N9qo8uLOickgx2ZMRZoHK.Z2PtJfDX7T5YdMh5VT5cVb5dLR5z5K2', 'LIDER', true),
    (1, 'Carlos López (Revisor)', 'carlos.lopez@techcorp.com', '$2a$10$N9qo8uLOickgx2ZMRZoHK.Z2PtJfDX7T5YdMh5VT5cVb5dLR5z5K2', 'REVISOR', true),
    (1, 'Ana Martínez', 'ana.martinez@techcorp.com', '$2a$10$N9qo8uLOickgx2ZMRZoHK.Z2PtJfDX7T5YdMh5VT5cVb5dLR5z5K2', 'USER', true),
    (2, 'Admin Innovate', 'admin@innovatelabs.io', '$2a$10$N9qo8uLOickgx2ZMRZoHK.Z2PtJfDX7T5YdMh5VT5cVb5dLR5z5K2', 'ADMIN', true);

-- Equipo de ejemplo
INSERT INTO equipo (empresa_id, lider_id, nombre, descripcion) VALUES
    (1, 2, 'Equipo Backend', 'Desarrollo de APIs y servicios');

-- Miembros del equipo
INSERT INTO miembros_equipo (equipo_id, usuario_id, rol) VALUES
    (1, 2, 'ADMIN'),
    (1, 3, 'MIEMBRO'),
    (1, 4, 'MIEMBRO');

-- Proyecto de ejemplo
INSERT INTO proyecto (empresa_id, lider_id, titulo, descripcion, fecha_fin_estimada) VALUES
    (1, 2, 'Sistema de Gestión de Tareas', 'Desarrollo del MVP para gestión de tareas multi-tenant', NOW() + INTERVAL '3 months');

-- Tareas de ejemplo
INSERT INTO tarea (empresa_id, proyecto_id, creador_id, usuario_asignado_id, titulo, descripcion, estado, prioridad, requiere_revision, fecha_limite) VALUES
    (1, 1, 1, 4, 'Implementar autenticación JWT', 'Crear sistema de login con tokens JWT', 'EN_PROCESO', 'ALTA', true, NOW() + INTERVAL '7 days'),
    (1, 1, 2, 4, 'Diseñar modelo de datos', 'Crear diagrama ER y scripts SQL', 'COMPLETADA', 'ALTA', false, NOW() - INTERVAL '3 days'),
    (1, 1, 2, NULL, 'Configurar CI/CD', 'Pipeline de integración continua', 'PENDIENTE', 'MEDIA', false, NOW() + INTERVAL '14 days');