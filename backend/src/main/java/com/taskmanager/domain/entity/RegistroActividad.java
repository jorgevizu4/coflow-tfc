package com.taskmanager.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entidad RegistroActividad - Auditoría AOP del sistema.
 * Almacena todas las acciones realizadas por los usuarios.
 */
@Entity
@Table(name = "registro_actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Campos AOP Auditing
    @Column(nullable = false, length = 100)
    private String entidad; // 'TAREA', 'PROYECTO', 'USUARIO', etc.

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(nullable = false, length = 50)
    private String accion; // 'CREATE', 'UPDATE', 'DELETE', 'ESTADO_CHANGE'

    @Column(length = 200)
    private String metodo; // Firma del método interceptado

    // Tracking de cambios (JSONB en PostgreSQL)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_anteriores", columnDefinition = "jsonb")
    private String valoresAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_nuevos", columnDefinition = "jsonb")
    private String valoresNuevos;

    // Contexto de la petición
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // Campos legacy (compatibilidad)
    @Column(name = "app_detectada", length = 100)
    private String appDetectada;

    @Column(length = 100)
    private String categoria;

    // Timestamps y retención
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (fechaExpiracion == null) {
            fechaExpiracion = LocalDateTime.now().plusDays(90); // Default: 90 días
        }
    }

    /**
     * Builder helper para crear registro de auditoría.
     */
    public static RegistroActividad crearAuditoria(
            Empresa empresa,
            Usuario usuario,
            String entidad,
            Long entidadId,
            String accion,
            String metodo,
            String valoresAnteriores,
            String valoresNuevos,
            String ipAddress) {

        return RegistroActividad.builder()
                .empresa(empresa)
                .usuario(usuario)
                .entidad(entidad)
                .entidadId(entidadId)
                .accion(accion)
                .metodo(metodo)
                .valoresAnteriores(valoresAnteriores)
                .valoresNuevos(valoresNuevos)
                .ipAddress(ipAddress)
                .build();
    }
}
