package com.taskmanager.scheduler;

import com.taskmanager.domain.entity.Tarea;
import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.repository.TareaRepository;
import com.taskmanager.repository.UsuarioRepository;
import com.taskmanager.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler para verificar tareas bloqueadas.
 * Ejecuta cada hora y notifica al líder si una tarea lleva más de 24h bloqueada.
 */
@Component
public class BloqueoScheduler {

    private static final Logger log = LoggerFactory.getLogger(BloqueoScheduler.class);
    private static final int HORAS_LIMITE_BLOQUEO = 24;

    private final TareaRepository tareaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificationService notificationService;

    public BloqueoScheduler(
            TareaRepository tareaRepository,
            UsuarioRepository usuarioRepository,
            NotificationService notificationService) {
        this.tareaRepository = tareaRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificationService = notificationService;
    }

    /**
     * Job que se ejecuta cada hora (minuto 0 de cada hora).
     * Busca tareas bloqueadas por más de 24 horas y notifica al líder.
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora en punto
    @Transactional
    public void verificarTareasBloqueadas() {
        log.info("Iniciando verificación de tareas bloqueadas...");

        LocalDateTime limiteBloqueo = LocalDateTime.now().minusHours(HORAS_LIMITE_BLOQUEO);
        List<Tarea> tareasBloqueadas = tareaRepository.findTareasBloqueadasMasDe(limiteBloqueo);

        if (tareasBloqueadas.isEmpty()) {
            log.info("No hay tareas bloqueadas por más de {} horas", HORAS_LIMITE_BLOQUEO);
            return;
        }

        log.warn("Encontradas {} tareas bloqueadas por más de {} horas", 
                tareasBloqueadas.size(), HORAS_LIMITE_BLOQUEO);

        for (Tarea tarea : tareasBloqueadas) {
            procesarTareaBloqueada(tarea);
        }

        log.info("Verificación de tareas bloqueadas completada");
    }

    private void procesarTareaBloqueada(Tarea tarea) {
        try {
            // Obtener el líder del proyecto
            Usuario lider = tarea.getProyecto().getLider();
            
            if (lider == null) {
                // Si no hay líder, buscar un admin de la empresa
                List<Usuario> admins = usuarioRepository.findLideresAndAdminsByEmpresaId(
                        tarea.getEmpresa().getId());
                
                if (!admins.isEmpty()) {
                    lider = admins.get(0);
                }
            }

            if (lider != null) {
                notificationService.notificarBloqueoExtendido(tarea, lider);
                log.info("Notificación enviada para tarea {} bloqueada desde {}", 
                        tarea.getId(), tarea.getFechaBloqueo());
            } else {
                log.warn("No se encontró líder/admin para notificar sobre tarea {} de empresa {}", 
                        tarea.getId(), tarea.getEmpresa().getId());
            }
            
        } catch (Exception e) {
            log.error("Error al procesar tarea bloqueada {}: {}", tarea.getId(), e.getMessage());
        }
    }
}
