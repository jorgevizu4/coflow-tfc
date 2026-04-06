package com.example.backend_v2.dto;

import com.example.backend_v2.model.entity.Tarea;
import com.example.backend_v2.model.enums.EstadoTarea;
import com.example.backend_v2.model.enums.Prioridad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TareaDTO {
    private Long id;
    private Long empresaId;
    private Long proyectoId;
    private String proyectoTitulo;
    private String titulo;
    private String descripcion;
    private EstadoTarea estado;
    private Prioridad prioridad;
    private boolean requiereRevision;
    private Integer tiempoEstimado;
    private int tiempoReal;
    private String fechaLimite;
    private String fechaBloqueo;
    private UsuarioResumidoDTO creador;
    private UsuarioResumidoDTO usuarioAsignado;
    private String createdAt;
    private String updatedAt;
    private int totalEntregables;
    private int totalComentarios;

    public static TareaDTO from(Tarea t) {
        String tituloTarea = t.getTitulo() != null ? t.getTitulo() : t.getNombre();
        Long empresaId = null;
        Long proyectoId = null;
        String proyectoTitulo = null;
        if (t.getProyecto() != null) {
            proyectoId = t.getProyecto().getId();
            if (t.getProyecto().getEmpresa() != null) {
                empresaId = t.getProyecto().getEmpresa().getId();
            }
            proyectoTitulo = t.getProyecto().getTitulo() != null
                    ? t.getProyecto().getTitulo()
                    : t.getProyecto().getNombre();
        }
        int comentarios = t.getListaComentarios() != null ? t.getListaComentarios().size() : 0;

        return TareaDTO.builder()
                .id(t.getId())
                .empresaId(empresaId)
                .proyectoId(proyectoId)
                .proyectoTitulo(proyectoTitulo)
                .titulo(tituloTarea)
                .descripcion(t.getDescripcion())
                .estado(t.getEstadoTarea())
                .prioridad(t.getPrioridad())
                .requiereRevision(t.isRequiereRevision())
                .tiempoEstimado(t.getTiempoEstimado())
                .tiempoReal(t.getTiempoReal())
                .fechaLimite(t.getFechaLimite() != null ? t.getFechaLimite() : t.getFechaFin())
                .fechaBloqueo(t.getFechaBloqueo())
                .creador(UsuarioResumidoDTO.from(t.getCreadoPor()))
                .usuarioAsignado(UsuarioResumidoDTO.from(t.getUsuarioAsignado()))
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .totalEntregables(0)
                .totalComentarios(comentarios)
                .build();
    }
}
