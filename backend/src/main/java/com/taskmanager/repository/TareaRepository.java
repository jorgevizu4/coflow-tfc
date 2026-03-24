package com.taskmanager.repository;

import com.taskmanager.domain.entity.Tarea;
import com.taskmanager.domain.enums.EstadoTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long>, JpaSpecificationExecutor<Tarea> {

       List<Tarea> findAllByEmpresaIdAndEliminadaFalse(Long empresaId);

      default List<Tarea> findAllByEmpresaId(Long empresaId) {
         return findAllByEmpresaIdAndEliminadaFalse(empresaId);
      }

       Optional<Tarea> findByIdAndEmpresaIdAndEliminadaFalse(Long id, Long empresaId);

      default Optional<Tarea> findByIdAndEmpresaId(Long id, Long empresaId) {
         return findByIdAndEmpresaIdAndEliminadaFalse(id, empresaId);
      }

       List<Tarea> findAllByProyectoIdAndEliminadaFalse(Long proyectoId);

       List<Tarea> findAllByEmpresaIdAndUsuarioAsignadoIdAndEliminadaFalse(Long empresaId, Long usuarioAsignadoId);

       List<Tarea> findAllByEmpresaIdAndEstadoAndEliminadaFalse(Long empresaId, EstadoTarea estado);

    /**
     * Busca tareas bloqueadas por más de las horas especificadas.
     */
       @Query("SELECT t FROM Tarea t WHERE t.eliminada = false AND t.estado = 'BLOQUEADA' AND t.fechaBloqueo < :limite")
    List<Tarea> findTareasBloqueadasMasDe(@Param("limite") LocalDateTime limite);

    /**
     * Busca tareas pendientes de revisión para una empresa.
     */
       @Query("SELECT t FROM Tarea t WHERE t.empresa.id = :empresaId AND t.eliminada = false AND t.estado = 'EN_REVISION'")
    List<Tarea> findTareasPendientesRevision(@Param("empresaId") Long empresaId);

    /**
     * Cuenta tareas por estado para una empresa.
     */
       @Query("SELECT t.estado, COUNT(t) FROM Tarea t WHERE t.empresa.id = :empresaId AND t.eliminada = false GROUP BY t.estado")
    List<Object[]> countByEstadoAndEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Busca tareas asignadas a un usuario que están próximas a vencer.
     */
    @Query("SELECT t FROM Tarea t WHERE t.eliminada = false AND t.usuarioAsignado.id = :usuarioId " +
           "AND t.fechaLimite BETWEEN :ahora AND :limite " +
           "AND t.estado NOT IN ('COMPLETADA', 'APROBADA', 'RECHAZADA')")
    List<Tarea> findTareasProximasAVencer(
            @Param("usuarioId") Long usuarioId,
            @Param("ahora") LocalDateTime ahora,
            @Param("limite") LocalDateTime limite);
}
