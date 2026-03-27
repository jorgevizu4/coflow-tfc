package com.example.backend_v2.model.entity;

import com.example.backend_v2.model.enums.EstadoTarea;
import com.example.backend_v2.model.enums.Prioridad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tarea")
public class Tarea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String titulo;
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private EstadoTarea estadoTarea;
    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    private String fechaInicio;
    private String fechaFin;
    private String fechaLimite;
    private String fechaBloqueo;

    private boolean requiereRevision;
    private Integer tiempoEstimado;
    private int tiempoReal;

    private String createdAt;
    private String updatedAt;

    @ManyToMany
    @JoinTable(
            name = "tarea_usuario",
            joinColumns = @JoinColumn(name = "tarea_id", foreignKey = @ForeignKey(name = "fk_tarea_usuario_tarea")),
            inverseJoinColumns = @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_tarea_usuario_usuario"))
    )
    private List<Usuario> usuarios;

    @ManyToOne
    @JoinColumn(name = "proyecto_id", foreignKey = @ForeignKey(name = "fk_tarea_proyecto"))
    private Proyecto proyecto;

    @ManyToOne
    @JoinColumn(name = "creado_por", foreignKey = @ForeignKey(name = "fk_tarea_creado_por"))
    private Usuario creadoPor;

    @ManyToOne
    @JoinColumn(name = "usuario_asignado_id", foreignKey = @ForeignKey(name = "fk_tarea_asignado"))
    private Usuario usuarioAsignado;

    @OneToMany(mappedBy = "tarea")
    private List<Comentario> listaComentarios;
}

