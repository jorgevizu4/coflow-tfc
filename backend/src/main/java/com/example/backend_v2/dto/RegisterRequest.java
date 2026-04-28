package com.example.backend_v2.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    private Long empresaId;
    private String nombre;
    private String apellidos;
    private String email;
    private String password;
    private String rol;
}
