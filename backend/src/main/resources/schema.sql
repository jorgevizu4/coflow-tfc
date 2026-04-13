SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS empresa_lista_usuarios;
DROP TABLE IF EXISTS tarea_usuario;
DROP TABLE IF EXISTS equipo_usuario;
DROP TABLE IF EXISTS proyecto_usuario;
DROP TABLE IF EXISTS comentario;
DROP TABLE IF EXISTS notificacion;
DROP TABLE IF EXISTS tarea;
DROP TABLE IF EXISTS proyecto;
DROP TABLE IF EXISTS equipo;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS empresa;

CREATE TABLE empresa (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(255),
    fecha_creacion DATE
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(255),
    apellidos      VARCHAR(255),
    email          VARCHAR(255) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    fecha_creacion DATE,
    rol            VARCHAR(50),
    empresa_id     BIGINT,
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id)
) ENGINE=InnoDB;

CREATE TABLE equipo (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(255),
    descripcion TEXT,
    empresa_id  BIGINT,
    proyecto_id BIGINT,
    CONSTRAINT fk_equipo_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id)
) ENGINE=InnoDB;

CREATE TABLE proyecto (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fecha_incio VARCHAR(50),
    fecha_fin   VARCHAR(50),
    empresa_id  BIGINT,
    equipo_id   BIGINT,
    CONSTRAINT fk_proyecto_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_proyecto_equipo  FOREIGN KEY (equipo_id)  REFERENCES equipo(id)
) ENGINE=InnoDB;

ALTER TABLE equipo
    ADD CONSTRAINT fk_equipo_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyecto(id);

CREATE TABLE tarea (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(255),
    descripcion  TEXT,
    estado_tarea VARCHAR(50),
    prioridad    VARCHAR(50),
    fecha_inicio VARCHAR(50),
    fecha_fin    VARCHAR(50),
    proyecto_id  BIGINT,
    creado_por   BIGINT,
    CONSTRAINT fk_tarea_proyecto    FOREIGN KEY (proyecto_id) REFERENCES proyecto(id),
    CONSTRAINT fk_tarea_creado_por  FOREIGN KEY (creado_por)  REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE TABLE notificacion (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    mensaje        TEXT,
    leida          BOOLEAN DEFAULT FALSE,
    fecha_creacion DATETIME,
    usuario_id     BIGINT,
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE TABLE comentario (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    contenido      TEXT,
    fecha_creacion DATETIME,
    usuario_id     BIGINT,
    tarea_id       BIGINT,
    CONSTRAINT fk_comentario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_comentario_tarea   FOREIGN KEY (tarea_id)   REFERENCES tarea(id)
) ENGINE=InnoDB;

CREATE TABLE equipo_usuario (
    equipo_id  BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (equipo_id, usuario_id),
    CONSTRAINT fk_equipo_usuario_equipo  FOREIGN KEY (equipo_id)  REFERENCES equipo(id),
    CONSTRAINT fk_equipo_usuario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE TABLE tarea_usuario (
    tarea_id   BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (tarea_id, usuario_id),
    CONSTRAINT fk_tarea_usuario_tarea   FOREIGN KEY (tarea_id)   REFERENCES tarea(id),
    CONSTRAINT fk_tarea_usuario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE TABLE proyecto_usuario (
    proyecto_id BIGINT NOT NULL,
    usuario_id  BIGINT NOT NULL,
    PRIMARY KEY (proyecto_id, usuario_id),
    CONSTRAINT fk_proyecto_usuario_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyecto(id),
    CONSTRAINT fk_proyecto_usuario_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario(id)
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;
