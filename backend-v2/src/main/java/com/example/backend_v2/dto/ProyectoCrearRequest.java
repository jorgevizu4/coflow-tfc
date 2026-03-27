package com.example.backend_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProyectoCrearRequest {
    private String titulo;
    private String descripcion;
    private String fechaFinEstimada;
    private Long liderId;
}
