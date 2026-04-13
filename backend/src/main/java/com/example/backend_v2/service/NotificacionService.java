package com.example.backend_v2.service;

import com.example.backend_v2.model.entity.Notificacion;
import com.example.backend_v2.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final AuthService authService;

    public List<Notificacion> findByUsuarioActual() {
        return notificacionRepository.findByUsuarioId(authService.getUsuarioActual().getId());
    }

    public List<Notificacion> findNoLeidasByUsuarioActual() {
        return notificacionRepository.findByUsuarioIdAndLeidaFalse(authService.getUsuarioActual().getId());
    }

    public Optional<Notificacion> findById(Long id) {
        return notificacionRepository.findById(id);
    }

    public Notificacion save(Notificacion notificacion) {
        return notificacionRepository.save(notificacion);
    }

    public void deleteById(Long id) {
        notificacionRepository.deleteById(id);
    }
}
