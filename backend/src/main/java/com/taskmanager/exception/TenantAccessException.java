package com.taskmanager.exception;

/**
 * Excepción para violaciones de aislamiento multi-tenant.
 */
public class TenantAccessException extends BusinessException {

    public TenantAccessException() {
        super("TENANT_ACCESS_DENIED", "Acceso denegado: el recurso no pertenece a su empresa");
    }

    public TenantAccessException(String entityName, Long id) {
        super("TENANT_ACCESS_DENIED", 
              String.format("Acceso denegado: %s con ID %d no pertenece a su empresa", entityName, id));
    }
}
