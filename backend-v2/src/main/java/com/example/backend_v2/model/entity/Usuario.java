package com.example.backend_v2.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.backend_v2.model.enums.RolUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellidos;
    private String email;
    private String password;
    private LocalDate fechaCreacion;

    @Enumerated(EnumType.STRING)
    private RolUsuario rol;

    @ManyToOne
    @JoinColumn(name = "empresa_id", foreignKey = @ForeignKey(name = "fk_usuario_empresa"))
    private Empresa empresa;

    @ManyToMany(mappedBy = "usuarios")
    private List<Equipo> listaEquipos;

    @ManyToMany(mappedBy = "usuarios")
    private List<Proyecto> listaProyectos;

    @ManyToMany(mappedBy = "usuarios")
    private List<Tarea> listaTareas;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = (rol != null) ? rol.name() : "USUARIO";
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
