package com.taskmanager.repository;

import com.taskmanager.domain.entity.RegistroActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroActividadRepository extends JpaRepository<RegistroActividad, Long>, JpaSpecificationExecutor<RegistroActividad> {

    List<RegistroActividad> findAllByEmpresaIdOrderByCreatedAtDesc(Long empresaId);

    List<RegistroActividad> findAllByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);

    List<RegistroActividad> findAllByEntidadAndEntidadIdOrderByCreatedAtDesc(String entidad, Long entidadId);

    /**
     * Elimina registros expirados (para job de limpieza).
     */
    @Modifying
    @Query("DELETE FROM RegistroActividad r WHERE r.fechaExpiracion < :ahora")
    int eliminarRegistrosExpirados(@Param("ahora") LocalDateTime ahora);

    /**
     * Cuenta registros que serán eliminados.
     */
    @Query("SELECT COUNT(r) FROM RegistroActividad r WHERE r.fechaExpiracion < :ahora")
    long countRegistrosExpirados(@Param("ahora") LocalDateTime ahora);
}
