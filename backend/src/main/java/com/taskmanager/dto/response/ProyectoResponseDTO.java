package com.taskmanager.dto.response;

import com.taskmanager.domain.entity.Proyecto;

import java.time.LocalDateTime;

/**
 * DTO de salida para proyectos.
 */
public record ProyectoResponseDTO(
        Long id,
        Long empresaId,
        String empresaNombre,
        String titulo,
        String descripcion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFinEstimada,
        Long liderId,
        String liderNombre
) {
    public static ProyectoResponseDTO fromEntity(Proyecto proyecto) {
        return new ProyectoResponseDTO(
                proyecto.getId(),
                proyecto.getEmpresa().getId(),
                proyecto.getEmpresa().getNombre(),
                proyecto.getTitulo(),
                proyecto.getDescripcion(),
                proyecto.getFechaInicio(),
                proyecto.getFechaFinEstimada(),
                proyecto.getLider() != null ? proyecto.getLider().getId() : null,
                proyecto.getLider() != null ? proyecto.getLider().getNombreCompleto() : null
        );
    }
}
