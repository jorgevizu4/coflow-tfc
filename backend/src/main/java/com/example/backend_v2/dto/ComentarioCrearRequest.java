package com.example.backend_v2.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ComentarioCrearRequest {
    private Long tareaId;
    private String contenido;
}
