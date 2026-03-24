package com.taskmanager.dto.request;

import com.taskmanager.domain.enums.EstadoTarea;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para mover una tarea entre estados desde tableros dinámicos.
 */
public record MoverEstadoDTO(
        @NotNull(message = "El estado destino es obligatorio")
        EstadoTarea estadoDestino,

        String comentario
) {}
