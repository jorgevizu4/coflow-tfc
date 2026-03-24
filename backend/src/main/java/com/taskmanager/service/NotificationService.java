package com.taskmanager.service;

import com.taskmanager.domain.entity.Notificacion;
import com.taskmanager.domain.entity.Tarea;
import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.domain.enums.TipoNotificacion;
import com.taskmanager.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de notificaciones.
 * Encapsula la creación y envío de notificaciones.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificacionRepository notificacionRepository;
    private final EmailService emailService;

    public NotificationService(NotificacionRepository notificacionRepository, EmailService emailService) {
        this.notificacionRepository = notificacionRepository;
        this.emailService = emailService;
    }

    /**
     * Notifica a un usuario que le han asignado una tarea.
     */
    @Transactional
    public void notificarAsignacion(Tarea tarea, Usuario usuario) {
        String mensaje = String.format(
                "Se te ha asignado la tarea '%s' con prioridad %s",
                tarea.getTitulo(),
                tarea.getPrioridad()
        );

        crearNotificacion(tarea, usuario, mensaje, TipoNotificacion.ASIGNACION);
        
        // Email opcional
        emailService.enviarNotificacionAsignacion(usuario.getEmail(), tarea);
        
        log.info("Notificación de asignación enviada a usuario {}", usuario.getId());
    }

    /**
     * Notifica al líder que una tarea ha sido bloqueada.
     */
    @Transactional
    public void notificarBloqueo(Tarea tarea, Usuario lider) {
        String mensaje = String.format(
                "La tarea '%s' ha sido bloqueada/rechazada por %s",
                tarea.getTitulo(),
                tarea.getUsuarioAsignado() != null ? tarea.getUsuarioAsignado().getNombreCompleto() : "N/A"
        );

        crearNotificacion(tarea, lider, mensaje, TipoNotificacion.ALERTA_BLOQUEO);
        
        log.info("Notificación de bloqueo enviada al líder {}", lider.getId());
    }

    /**
     * Notifica que una tarea ha sido aprobada.
     */
    @Transactional
    public void notificarAprobacion(Tarea tarea) {
        if (tarea.getUsuarioAsignado() == null) return;

        String mensaje = String.format(
                "Tu entrega de la tarea '%s' ha sido aprobada",
                tarea.getTitulo()
        );

        crearNotificacion(tarea, tarea.getUsuarioAsignado(), mensaje, TipoNotificacion.APROBACION);
        
        log.info("Notificación de aprobación enviada a usuario {}", tarea.getUsuarioAsignado().getId());
    }

    /**
     * Notifica que una tarea ha sido rechazada en revisión.
     */
    @Transactional
    public void notificarRechazoRevision(Tarea tarea, String motivo) {
        if (tarea.getUsuarioAsignado() == null) return;

        String mensaje = String.format(
                "Tu entrega de la tarea '%s' ha sido rechazada. Motivo: %s",
                tarea.getTitulo(),
                motivo
        );

        crearNotificacion(tarea, tarea.getUsuarioAsignado(), mensaje, TipoNotificacion.RECHAZO);
        
        log.info("Notificación de rechazo enviada a usuario {}", tarea.getUsuarioAsignado().getId());
    }

    /**
     * Notifica que una tarea está lista para revisión.
     */
    @Transactional
    public void notificarListaParaRevision(Tarea tarea, Usuario revisor) {
        String mensaje = String.format(
                "La tarea '%s' está lista para revisión",
                tarea.getTitulo()
        );

        crearNotificacion(tarea, revisor, mensaje, TipoNotificacion.REVISION);
        
        log.info("Notificación de revisión pendiente enviada a revisor {}", revisor.getId());
    }

    /**
     * Notifica que una tarea lleva bloqueada más de 24 horas (para scheduler).
     */
    @Transactional
    public void notificarBloqueoExtendido(Tarea tarea, Usuario lider) {
        String mensaje = String.format(
                "ALERTA: La tarea '%s' lleva más de 24 horas bloqueada. " +
                "Usuario asignado: %s",
                tarea.getTitulo(),
                tarea.getUsuarioAsignado() != null ? tarea.getUsuarioAsignado().getNombreCompleto() : "N/A"
        );

        crearNotificacion(tarea, lider, mensaje, TipoNotificacion.ALERTA_BLOQUEO);
        
        // Enviar email de alerta al líder
        emailService.enviarAlertaBloqueo(lider.getEmail(), tarea);
        
        log.warn("ALERTA: Tarea {} bloqueada más de 24h, notificado líder {}", 
                tarea.getId(), lider.getId());
    }

    // ============ Métodos Privados ============

    private void crearNotificacion(Tarea tarea, Usuario usuario, String mensaje, TipoNotificacion tipo) {
        Notificacion notificacion = Notificacion.builder()
                .empresa(tarea.getEmpresa())
                .usuario(usuario)
                .tarea(tarea)
                .mensaje(mensaje)
                .tipo(tipo)
                .leida(false)
                .build();

        notificacionRepository.save(notificacion);
    }
}
