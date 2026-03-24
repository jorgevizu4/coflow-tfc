package com.taskmanager.repository;

import com.taskmanager.domain.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long>, JpaSpecificationExecutor<Notificacion> {

    List<Notificacion> findAllByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Notificacion> findAllByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(Long usuarioId);

    List<Notificacion> findAllByEmpresaId(Long empresaId);

    Optional<Notificacion> findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Cuenta notificaciones no leídas de un usuario.
     */
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    /**
     * Marca todas las notificaciones de un usuario como leídas.
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario.id = :usuarioId AND n.leida = false")
    int marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId);
}
