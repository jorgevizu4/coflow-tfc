package com.taskmanager.dto.response;

import com.taskmanager.domain.entity.Tarea;
import com.taskmanager.domain.enums.EstadoTarea;
import com.taskmanager.domain.enums.Prioridad;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para tareas.
 */
public record TareaResponseDTO(
        Long id,
        Long empresaId,
        Long proyectoId,
        String proyectoTitulo,
        String titulo,
        String descripcion,
        EstadoTarea estado,
        Prioridad prioridad,
        Boolean requiereRevision,
        Integer tiempoEstimado,
        Integer tiempoReal,
        LocalDateTime fechaLimite,
        LocalDateTime fechaBloqueo,
        UsuarioResumidoDTO creador,
        UsuarioResumidoDTO usuarioAsignado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int totalEntregables,
        int totalComentarios
) {
    /**
     * DTO resumido de usuario para evitar referencias circulares.
     */
    public record UsuarioResumidoDTO(
            Long id,
            String nombreCompleto,
            String email
    ) {
        public static UsuarioResumidoDTO fromEntity(com.taskmanager.domain.entity.Usuario usuario) {
            if (usuario == null) return null;
            return new UsuarioResumidoDTO(
                    usuario.getId(),
                    usuario.getNombreCompleto(),
                    usuario.getEmail()
            );
        }
    }

    /**
     * Convierte una entidad Tarea a DTO.
     */
    public static TareaResponseDTO fromEntity(Tarea tarea) {
        return new TareaResponseDTO(
                tarea.getId(),
                tarea.getEmpresa().getId(),
                tarea.getProyecto().getId(),
                tarea.getProyecto().getTitulo(),
                tarea.getTitulo(),
                tarea.getDescripcion(),
                tarea.getEstado(),
                tarea.getPrioridad(),
                tarea.getRequiereRevision(),
                tarea.getTiempoEstimado(),
                tarea.getTiempoReal(),
                tarea.getFechaLimite(),
                tarea.getFechaBloqueo(),
                UsuarioResumidoDTO.fromEntity(tarea.getCreador()),
                UsuarioResumidoDTO.fromEntity(tarea.getUsuarioAsignado()),
                tarea.getCreatedAt(),
                tarea.getUpdatedAt(),
                tarea.getEntregables() != null ? tarea.getEntregables().size() : 0,
                tarea.getComentarios() != null ? tarea.getComentarios().size() : 0
        );
    }

    /**
     * Convierte una lista de entidades a lista de DTOs.
     */
    public static List<TareaResponseDTO> fromEntities(List<Tarea> tareas) {
        return tareas.stream()
                .map(TareaResponseDTO::fromEntity)
                .toList();
    }
}
