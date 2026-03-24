package com.taskmanager.repository;

import com.taskmanager.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

    List<Usuario> findAllByEmpresaId(Long empresaId);

    List<Usuario> findAllByEmpresaIdAndActivoTrue(Long empresaId);

    boolean existsByEmpresaIdAndEmail(Long empresaId, String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.empresa.id = :empresaId AND u.rol IN ('LIDER', 'ADMIN')")
    List<Usuario> findLideresAndAdminsByEmpresaId(@Param("empresaId") Long empresaId);
}
