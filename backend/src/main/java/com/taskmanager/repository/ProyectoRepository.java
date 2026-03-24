package com.taskmanager.repository;

import com.taskmanager.domain.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long>, JpaSpecificationExecutor<Proyecto> {

    List<Proyecto> findAllByEmpresaId(Long empresaId);

    Optional<Proyecto> findByIdAndEmpresaId(Long id, Long empresaId);

    List<Proyecto> findAllByLiderId(Long liderId);
}
