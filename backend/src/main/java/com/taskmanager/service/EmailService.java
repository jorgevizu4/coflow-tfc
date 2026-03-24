package com.taskmanager.service;

import com.taskmanager.domain.entity.Tarea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio de envío de emails.
 * Implementación simulada para MVP.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@taskmanager.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía notificación de asignación de tarea.
     */
    @Async
    public void enviarNotificacionAsignacion(String email, Tarea tarea) {
        String subject = "Nueva tarea asignada: " + tarea.getTitulo();
        String body = String.format(
                """
                Se te ha asignado una nueva tarea:
                
                Título: %s
                Descripción: %s
                Prioridad: %s
                Fecha límite: %s
                
                Por favor, accede al sistema para revisar los detalles.
                """,
                tarea.getTitulo(),
                tarea.getDescripcion() != null ? tarea.getDescripcion() : "Sin descripción",
                tarea.getPrioridad(),
                tarea.getFechaLimite() != null ? tarea.getFechaLimite().toString() : "Sin fecha límite"
        );

        enviarEmail(email, subject, body);
    }

    /**
     * Envía alerta de tarea bloqueada más de 24 horas.
     */
    @Async
    public void enviarAlertaBloqueo(String email, Tarea tarea) {
        String subject = "⚠️ ALERTA: Tarea bloqueada más de 24h - " + tarea.getTitulo();
        String body = String.format(
                """
                ALERTA: Una tarea lleva bloqueada más de 24 horas
                
                Título: %s
                Usuario asignado: %s
                Fecha de bloqueo: %s
                
                Por favor, toma acción para resolver el bloqueo.
                """,
                tarea.getTitulo(),
                tarea.getUsuarioAsignado() != null ? tarea.getUsuarioAsignado().getNombreCompleto() : "N/A",
                tarea.getFechaBloqueo() != null ? tarea.getFechaBloqueo().toString() : "N/A"
        );

        enviarEmail(email, subject, body);
    }

    /**
     * Envía email genérico.
     */
    public void enviarEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("[TaskManager] " + subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email enviado a {}: {}", to, subject);
            
        } catch (Exception e) {
            // En desarrollo, solo loguear el error (MailHog puede no estar activo)
            log.warn("No se pudo enviar email a {}: {} - {}", to, subject, e.getMessage());
        }
    }
}
