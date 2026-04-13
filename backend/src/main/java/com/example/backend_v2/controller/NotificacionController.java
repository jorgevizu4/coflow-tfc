package com.example.backend_v2.controller;

import com.example.backend_v2.model.entity.Notificacion;
import com.example.backend_v2.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public List<Notificacion> findAll() {
        return notificacionService.findByUsuarioActual();
    }

    @GetMapping("/no-leidas")
    public List<Notificacion> findNoLeidas() {
        return notificacionService.findNoLeidasByUsuarioActual();
    }

    @PostMapping("/{id}/leer")
    public void marcarComoLeida(@PathVariable Long id) {
        notificacionService.findById(id).ifPresent(n -> {
            n.setLeida(true);
            notificacionService.save(n);
        });
    }
}
