package com.taskmanager.domain.entity;

import com.taskmanager.domain.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Usuario con soporte multi-tenant y roles.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RolUsuario rol = RolUsuario.USER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "estado_actual", length = 50)
    private String estadoActual;

    @Column(name = "configuracion_ia", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String configuracionIa;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    // Relaciones inversas
    @OneToMany(mappedBy = "usuarioAsignado")
    @Builder.Default
    private List<Tarea> tareasAsignadas = new ArrayList<>();

    @OneToMany(mappedBy = "creador")
    @Builder.Default
    private List<Tarea> tareasCreadas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    /**
     * Verifica si el usuario puede aprobar/rechazar tareas.
     */
    public boolean puedeRevisar() {
        return rol == RolUsuario.ADMIN || rol == RolUsuario.LIDER || rol == RolUsuario.REVISOR;
    }

    /**
     * Verifica si el usuario puede asignar tareas.
     */
    public boolean puedeAsignar() {
        return rol == RolUsuario.ADMIN || rol == RolUsuario.LIDER;
    }

    /**
     * Verifica si el usuario es administrador.
     */
    public boolean esAdmin() {
        return rol == RolUsuario.ADMIN;
    }
}
