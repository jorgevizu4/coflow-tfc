package com.taskmanager.dto.request;

import com.taskmanager.domain.enums.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para crear una nueva tarea.
 */
public record TareaCreateDTO(
        @NotNull(message = "El proyecto es obligatorio") Long proyectoId,

        @NotBlank(message = "El título es obligatorio") @Size(max = 200, message = "El título no puede exceder 200 caracteres") String titulo,

        String descripcion,

        Integer tiempoEstimado,

        LocalDateTime fechaLimite,

        Prioridad prioridad,

        Boolean requiereRevision) {
    public TareaCreateDTO {
        // Valores por defecto
        if (prioridad == null) {
            prioridad = Prioridad.MEDIA;
        }
        if (requiereRevision == null) {
            requiereRevision = false;
        }
    }
}
