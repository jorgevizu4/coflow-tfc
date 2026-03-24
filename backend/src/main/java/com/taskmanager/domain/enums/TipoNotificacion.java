package com.taskmanager.domain.enums;

/**
 * Tipos de notificación del sistema.
 */
public enum TipoNotificacion {
    ALERTA_BLOQUEO,     // Tarea bloqueada más de 24h
    ASIGNACION,         // Nueva tarea asignada
    REVISION,           // Tarea lista para revisión
    APROBACION,         // Tarea aprobada
    RECHAZO,            // Tarea rechazada
    COMENTARIO          // Nuevo comentario en tarea
}
