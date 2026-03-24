package com.taskmanager.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para cambio de estado de tarea (aceptar/rechazar).
 */
public record CambioEstadoDTO(
        @NotNull(message = "La acción es obligatoria")
        AccionEstado accion,

        String motivo // Opcional, para rechazos
) {
    public enum AccionEstado {
        ACEPTAR,
        RECHAZAR
    }
}
