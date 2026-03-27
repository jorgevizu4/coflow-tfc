package com.example.backend_v2.dto;

import com.example.backend_v2.model.enums.Prioridad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TareaCrearRequest {
    private Long proyectoId;
    private String titulo;
    private String descripcion;
    private Integer tiempoEstimado;
    private String fechaLimite;
    private Prioridad prioridad;
    private Boolean requiereRevision;
}
