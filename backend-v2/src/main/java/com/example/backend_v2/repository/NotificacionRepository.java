package com.example.backend_v2.repository;

import com.example.backend_v2.model.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioId(Long usuarioId);

    List<Notificacion> findByUsuarioIdAndLeidaFalse(Long usuarioId);
}
