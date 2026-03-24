package com.taskmanager.exception;

/**
 * Excepción cuando el usuario no tiene permisos para una acción.
 */
public class ForbiddenRoleException extends BusinessException {

    public ForbiddenRoleException(String action) {
        super("FORBIDDEN_ROLE", String.format("No tiene permisos para: %s", action));
    }

    public ForbiddenRoleException(String action, String requiredRole) {
        super("FORBIDDEN_ROLE", String.format("Se requiere rol '%s' para: %s", requiredRole, action));
    }
}
