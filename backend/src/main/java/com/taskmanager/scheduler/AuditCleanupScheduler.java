package com.taskmanager.scheduler;

import com.taskmanager.repository.RegistroActividadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduler para limpieza de registros de auditoría expirados.
 * Ejecuta diariamente a las 2 AM y elimina registros cuya fecha_expiracion ha pasado.
 */
@Component
public class AuditCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuditCleanupScheduler.class);

    private final RegistroActividadRepository registroRepository;

    @Value("${app.audit.retention-days:90}")
    private int retentionDays;

    public AuditCleanupScheduler(RegistroActividadRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    /**
     * Job de limpieza que se ejecuta diariamente a las 2 AM.
     * Elimina registros de auditoría cuya fecha de expiración ha pasado.
     */
    @Scheduled(cron = "0 0 2 * * *") // Cada día a las 2:00 AM
    @Transactional
    public void limpiarRegistrosExpirados() {
        log.info("Iniciando limpieza de registros de auditoría expirados...");

        LocalDateTime ahora = LocalDateTime.now();

        // Contar registros a eliminar
        long totalExpirados = registroRepository.countRegistrosExpirados(ahora);
        
        if (totalExpirados == 0) {
            log.info("No hay registros de auditoría expirados para eliminar");
            return;
        }

        log.info("Encontrados {} registros de auditoría expirados", totalExpirados);

        // Eliminar registros expirados
        int eliminados = registroRepository.eliminarRegistrosExpirados(ahora);

        log.info("Limpieza completada: {} registros de auditoría eliminados", eliminados);
    }

    /**
     * Método para ejecutar limpieza manualmente (útil para testing o mantenimiento).
     */
    @Transactional
    public int limpiarManualmente() {
        log.info("Ejecutando limpieza manual de registros de auditoría...");
        int eliminados = registroRepository.eliminarRegistrosExpirados(LocalDateTime.now());
        log.info("Limpieza manual completada: {} registros eliminados", eliminados);
        return eliminados;
    }
}
