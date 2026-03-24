package com.taskmanager.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Subtarea - Tareas hijas de una tarea principal.
 */
@Entity
@Table(name = "subtarea")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subtarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_padre_id", nullable = false)
    private Tarea tarea;

    @Column(length = 200)
    private String titulo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completada = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
