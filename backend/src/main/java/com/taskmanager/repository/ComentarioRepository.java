package com.taskmanager.repository;

import com.taskmanager.domain.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long>, JpaSpecificationExecutor<Comentario> {

    List<Comentario> findAllByTareaIdOrderByFechaCreacionDesc(Long tareaId);

    List<Comentario> findAllByEmpresaId(Long empresaId);

    Optional<Comentario> findByIdAndEmpresaId(Long id, Long empresaId);
}
