package com.example.backend_v2.model.entity;

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
@Table(name = "proyecto")
public class Proyecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;
    private String titulo;
    private String descripcion;
    private String fechaIncio;
    private String fechaFin;
    private String fechaFinEstimada;

    @ManyToOne
    @JoinColumn(name = "empresa_id", foreignKey = @ForeignKey(name = "fk_proyecto_empresa"))
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "lider_id", foreignKey = @ForeignKey(name = "fk_proyecto_lider"))
    private Usuario lider;

    @OneToMany(mappedBy = "proyecto")
    private List<Tarea> listaTareas;

    @ManyToMany
    @JoinTable(
            name = "proyecto_usuario",
            joinColumns = @JoinColumn(name = "proyecto_id", foreignKey = @ForeignKey(name = "fk_proyecto_usuario_proyecto")),
            inverseJoinColumns = @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_proyecto_usuario_usuario"))
    )
    private List<Usuario> usuarios;

    @ManyToOne
    @JoinColumn(name = "equipo_id", foreignKey = @ForeignKey(name = "fk_proyecto_equipo"))
    private Equipo equipo;
}

