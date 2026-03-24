package com.taskmanager.repository;

import com.taskmanager.domain.entity.Entregable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregableRepository extends JpaRepository<Entregable, Long>, JpaSpecificationExecutor<Entregable> {

    List<Entregable> findAllByTareaId(Long tareaId);

    List<Entregable> findAllByEmpresaId(Long empresaId);

    Optional<Entregable> findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Encuentra la última versión de un entregable para una tarea.
     */
    Optional<Entregable> findFirstByTareaIdOrderByVersionDesc(Long tareaId);

    /**
     * Cuenta entregables por tarea.
     */
    long countByTareaId(Long tareaId);
}
