package com.example.backend_v2.dto;

import com.example.backend_v2.model.entity.Proyecto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProyectoDTO {
    private Long id;
    private Long empresaId;
    private String empresaNombre;
    private String titulo;
    private String descripcion;
    private String fechaInicio;
    private String fechaFinEstimada;
    private Long liderId;
    private String liderNombre;

    public static ProyectoDTO from(Proyecto p) {
        String titulo = p.getTitulo() != null ? p.getTitulo() : p.getNombre();
        String fechaFin = p.getFechaFinEstimada() != null ? p.getFechaFinEstimada() : p.getFechaFin();
        Long liderId = null;
        String liderNombre = null;
        if (p.getLider() != null) {
            liderId = p.getLider().getId();
            String nombre = p.getLider().getNombre() != null ? p.getLider().getNombre() : "";
            String apellidos = p.getLider().getApellidos() != null ? " " + p.getLider().getApellidos() : "";
            liderNombre = (nombre + apellidos).trim();
        }
        return ProyectoDTO.builder()
                .id(p.getId())
                .empresaId(p.getEmpresa() != null ? p.getEmpresa().getId() : null)
                .empresaNombre(p.getEmpresa() != null ? p.getEmpresa().getNombre() : null)
                .titulo(titulo)
                .descripcion(p.getDescripcion())
                .fechaInicio(p.getFechaIncio())
                .fechaFinEstimada(fechaFin)
                .liderId(liderId)
                .liderNombre(liderNombre)
                .build();
    }
}
