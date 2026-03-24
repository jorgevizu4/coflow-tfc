package com.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para crear proyectos.
 */
public record ProyectoCreateDTO(
        @NotBlank(message = "El título del proyecto es obligatorio")
        @Size(max = 200, message = "El título no puede superar 200 caracteres")
        String titulo,

        @Size(max = 5000, message = "La descripción no puede superar 5000 caracteres")
        String descripcion,

        LocalDateTime fechaFinEstimada,

        Long liderId
) {}
