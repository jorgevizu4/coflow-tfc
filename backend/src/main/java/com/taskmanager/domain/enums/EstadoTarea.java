package com.taskmanager.domain.enums;

/**
 * Estados posibles de una tarea en el flujo de trabajo.
 * Mapea directamente al ENUM estado_tarea_enum de PostgreSQL.
 */
public enum EstadoTarea {
    PENDIENTE,      // Tarea creada, sin asignar
    ASIGNADA,       // Tarea asignada a un usuario
    EN_PROCESO,     // Usuario aceptó y está trabajando
    BLOQUEADA,      // Usuario rechazó o hay impedimentos
    EN_REVISION,    // Entregable subido, esperando aprobación
    APROBADA,       // Líder/Revisor aprobó el trabajo
    RECHAZADA,      // Líder/Revisor rechazó el trabajo
    COMPLETADA      // Tarea finalizada
}
