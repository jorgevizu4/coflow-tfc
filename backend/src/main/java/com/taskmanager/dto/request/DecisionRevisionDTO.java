package com.taskmanager.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para decisión de revisión (aprobar/rechazar trabajo).
 */
public record DecisionRevisionDTO(
        @NotNull(message = "Debe indicar si aprueba o rechaza")
        Boolean aprobado,

        String comentario // Obligatorio si rechaza
) {}
