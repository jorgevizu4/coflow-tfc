package com.example.backend_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CambioEstadoRequest {
    private String accion; // ACEPTAR | RECHAZAR
    private String motivo;
}
