package com.taskmanager.dto.response;

import com.taskmanager.domain.entity.Entregable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para entregables.
 */
public record EntregableResponseDTO(
        Long id,
        Long tareaId,
        String nombreArchivo,
        String urlArchivo,
        String tipoContenido,
        Long tamanoBytes,
        String comentarios,
        Integer version,
        LocalDateTime fechaSubida,
        TareaResponseDTO.UsuarioResumidoDTO usuario
) {
    public static EntregableResponseDTO fromEntity(Entregable entregable) {
        return new EntregableResponseDTO(
                entregable.getId(),
                entregable.getTarea().getId(),
                entregable.getNombreArchivo(),
                entregable.getUrlArchivo(),
                entregable.getTipoContenido(),
                entregable.getTamanoBytes(),
                entregable.getComentarios(),
                entregable.getVersion(),
                entregable.getFechaSubida(),
                TareaResponseDTO.UsuarioResumidoDTO.fromEntity(entregable.getUsuario())
        );
    }

    public static List<EntregableResponseDTO> fromEntities(List<Entregable> entregables) {
        return entregables.stream()
                .map(EntregableResponseDTO::fromEntity)
                .toList();
    }
}
