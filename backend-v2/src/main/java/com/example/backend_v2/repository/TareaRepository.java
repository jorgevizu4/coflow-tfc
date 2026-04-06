package com.example.backend_v2.repository;

import com.example.backend_v2.model.entity.Tarea;
import com.example.backend_v2.model.enums.EstadoTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByProyectoId(long proyectoId);
    List<Tarea> findByUsuariosId(long usuarioId);
    List<Tarea> findByUsuarioAsignadoId(long usuarioId);

    @Query("SELECT t FROM Tarea t JOIN t.proyecto p WHERE p.empresa.id = :empresaId")
    List<Tarea> findByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT t FROM Tarea t JOIN t.proyecto p WHERE p.empresa.id = :empresaId AND t.estadoTarea = :estado")
    List<Tarea> findByEmpresaIdAndEstado(@Param("empresaId") Long empresaId, @Param("estado") EstadoTarea estado);

    @Query("SELECT t FROM Tarea t JOIN t.proyecto p WHERE p.empresa.id = :empresaId AND t.usuarioAsignado.id = :usuarioId")
    List<Tarea> findMisTareas(@Param("empresaId") Long empresaId, @Param("usuarioId") Long usuarioId);
}

