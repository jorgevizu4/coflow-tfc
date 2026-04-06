package com.example.backend_v2.dto;

import com.example.backend_v2.model.enums.Prioridad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignacionRequest {
    private Long usuarioAsignadoId;
    private Prioridad prioridad;
    private String fechaLimite;
}
