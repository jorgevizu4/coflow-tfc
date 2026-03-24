package com.taskmanager.domain.enums;

/**
 * Roles de usuario para control de acceso.
 * Mapea directamente al ENUM rol_usuario_enum de PostgreSQL.
 */
public enum RolUsuario {
    ADMIN,      // Administrador de la empresa
    LIDER,      // Líder de equipo/proyecto - puede aprobar tareas
    REVISOR,    // Puede revisar y aprobar tareas
    USER        // Usuario estándar
}
