package com.taskmanager.domain.entity;

import com.taskmanager.domain.enums.EstadoTarea;
import com.taskmanager.domain.enums.Prioridad;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Tarea - Núcleo del sistema de gestión.
 * Incluye máquina de estados y soporte multi-tenant.
 */
@Entity
@Table(name = "tarea")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_asignado_id")
    private Usuario usuarioAsignado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    private Usuario creador;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Control de tiempos
    @Column(name = "tiempo_estimado")
    private Integer tiempoEstimado; // En minutos

    @Column(name = "tiempo_real")
    @Builder.Default
    private Integer tiempoReal = 0;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    // Control de flujo y estados
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_tarea_enum")
    @Builder.Default
    private EstadoTarea estado = EstadoTarea.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "prioridad_enum")
    @Builder.Default
    private Prioridad prioridad = Prioridad.MEDIA;

    @Column(name = "requiere_revision", nullable = false)
    @Builder.Default
    private Boolean requiereRevision = false;

    @Column(name = "eliminada", nullable = false)
    @Builder.Default
    private Boolean eliminada = false;

    @Column(name = "eliminada_at")
    private LocalDateTime eliminadaAt;

    @Column(name = "eliminada_por_usuario_id")
    private Long eliminadaPorUsuarioId;

    // Relaciones
    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Entregable> entregables = new ArrayList<>();

    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comentario> comentarios = new ArrayList<>();

    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subtarea> subtareas = new ArrayList<>();

    // ==================== Métodos de máquina de estados ====================

    /**
     * Asigna la tarea a un usuario y cambia estado a ASIGNADA.
     */
    public void asignar(Usuario usuario, Prioridad prioridad, LocalDateTime fechaLimite) {
        if (this.estado != EstadoTarea.PENDIENTE && this.estado != EstadoTarea.RECHAZADA) {
            throw new IllegalStateException("Solo se pueden asignar tareas PENDIENTES o RECHAZADAS");
        }
        this.usuarioAsignado = usuario;
        this.prioridad = prioridad;
        this.fechaLimite = fechaLimite;
        this.estado = EstadoTarea.ASIGNADA;
    }

    /**
     * Usuario acepta la tarea asignada.
     */
    public void aceptar() {
        if (this.estado != EstadoTarea.ASIGNADA) {
            throw new IllegalStateException("Solo se pueden aceptar tareas ASIGNADAS");
        }
        this.estado = EstadoTarea.EN_PROCESO;
    }

    /**
     * Usuario rechaza la tarea (la bloquea).
     */
    public void rechazar() {
        if (this.estado != EstadoTarea.ASIGNADA) {
            throw new IllegalStateException("Solo se pueden rechazar tareas ASIGNADAS");
        }
        this.estado = EstadoTarea.BLOQUEADA;
        this.fechaBloqueo = LocalDateTime.now();
    }

    /**
     * Marca la tarea como completada o envía a revisión.
     */
    public void entregarTrabajo() {
        if (this.estado != EstadoTarea.EN_PROCESO) {
            throw new IllegalStateException("Solo se puede entregar trabajo en tareas EN_PROCESO");
        }
        if (this.requiereRevision) {
            this.estado = EstadoTarea.EN_REVISION;
        } else {
            this.estado = EstadoTarea.COMPLETADA;
        }
    }

    /**
     * Líder/Revisor aprueba el trabajo.
     */
    public void aprobar() {
        if (this.estado != EstadoTarea.EN_REVISION) {
            throw new IllegalStateException("Solo se pueden aprobar tareas EN_REVISION");
        }
        this.estado = EstadoTarea.APROBADA;
    }

    /**
     * Líder/Revisor rechaza el trabajo y lo devuelve.
     */
    public void rechazarRevision() {
        if (this.estado != EstadoTarea.EN_REVISION) {
            throw new IllegalStateException("Solo se pueden rechazar revisiones de tareas EN_REVISION");
        }
        this.estado = EstadoTarea.EN_PROCESO; // Vuelve al usuario para correcciones
    }

    /**
     * Desbloquea una tarea bloqueada.
     */
    public void desbloquear() {
        if (this.estado != EstadoTarea.BLOQUEADA) {
            throw new IllegalStateException("Solo se pueden desbloquear tareas BLOQUEADAS");
        }
        this.estado = EstadoTarea.PENDIENTE;
        this.fechaBloqueo = null;
        this.usuarioAsignado = null;
    }

    /**
     * Verifica si la tarea está bloqueada más de 24 horas.
     */
    public boolean estaBloqueadaMasDe24Horas() {
        if (this.estado != EstadoTarea.BLOQUEADA || this.fechaBloqueo == null) {
            return false;
        }
        return this.fechaBloqueo.plusHours(24).isBefore(LocalDateTime.now());
    }
}
