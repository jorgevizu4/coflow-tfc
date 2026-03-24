package com.taskmanager.dto.request;

import com.taskmanager.domain.enums.Prioridad;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO para asignar una tarea a un usuario.
 */
public record AsignacionDTO(
        @NotNull(message = "El usuario asignado es obligatorio")
        Long usuarioAsignadoId,

        Prioridad prioridad,

        LocalDateTime fechaLimite
) {
    public AsignacionDTO {
        if (prioridad == null) {
            prioridad = Prioridad.MEDIA;
        }
    }
}
