package com.example.backend_v2.repository;

import com.example.backend_v2.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findByEmpresaId(long empresaId);
    Optional<Usuario> findByEmail(String email);
}
