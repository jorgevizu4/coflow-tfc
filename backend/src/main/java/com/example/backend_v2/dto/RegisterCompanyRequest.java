package com.example.backend_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterCompanyRequest {
    private String nombreEmpresa;
    private String nombreAdministrador;
    private String emailAdministrador;
    private String passwordAdministrador;
}
