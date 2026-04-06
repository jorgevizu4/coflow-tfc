package com.example.backend_v2.dto;

import com.example.backend_v2.model.enums.EstadoTarea;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoverEstadoRequest {
    private EstadoTarea estadoDestino;
    private String comentario;
}
