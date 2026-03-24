package com.taskmanager.exception;

/**
 * Excepción para transiciones de estado inválidas.
 */
public class InvalidStateTransitionException extends BusinessException {

    public InvalidStateTransitionException(String currentState, String attemptedAction) {
        super("INVALID_STATE_TRANSITION", 
              String.format("No se puede '%s' una tarea en estado '%s'", attemptedAction, currentState));
    }
}
