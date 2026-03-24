package com.taskmanager.dto.response;

import com.taskmanager.domain.entity.Empresa;

import java.time.LocalDateTime;

/**
 * DTO de salida para empresa.
 */
public record EmpresaResponseDTO(
        Long id,
        String nombre,
        LocalDateTime fechaCreacion
) {
    public static EmpresaResponseDTO fromEntity(Empresa empresa) {
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getFechaCreacion()
        );
    }
}
