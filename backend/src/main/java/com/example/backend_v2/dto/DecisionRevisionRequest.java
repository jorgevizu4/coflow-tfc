package com.example.backend_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecisionRevisionRequest {
    private Boolean aprobado;
    private String comentario;
}
