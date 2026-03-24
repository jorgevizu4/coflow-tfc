package com.taskmanager.exception;

/**
 * Excepción cuando no se encuentra una entidad.
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String entityName, Long id) {
        super("ENTITY_NOT_FOUND", String.format("%s con ID %d no encontrado", entityName, id));
    }

    public EntityNotFoundException(String entityName, String identifier) {
        super("ENTITY_NOT_FOUND", String.format("%s '%s' no encontrado", entityName, identifier));
    }
}
