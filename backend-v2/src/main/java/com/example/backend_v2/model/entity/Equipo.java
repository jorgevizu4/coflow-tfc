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
@Table(name = "equipo")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "empresa_id", foreignKey = @ForeignKey(name = "fk_equipo_empresa"))
    private Empresa empresa;

    @ManyToMany
    @JoinTable(
            name = "equipo_usuario",
            joinColumns = @JoinColumn(name = "equipo_id", foreignKey = @ForeignKey(name = "fk_equipo_usuario_equipo")),
            inverseJoinColumns = @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_equipo_usuario_usuario"))
    )
    private List<Usuario> usuarios;

    @ManyToOne
    @JoinColumn(name = "proyecto_id", foreignKey = @ForeignKey(name = "fk_equipo_proyecto"))
    private Proyecto proyecto;

}
