package com.taskmanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para registrar una empresa con su usuario administrador inicial.
 */
public record EmpresaRegistroDTO(
        @NotBlank(message = "El nombre de empresa es obligatorio")
        @Size(max = 100, message = "El nombre de empresa no puede superar 100 caracteres")
        String nombreEmpresa,

        @NotBlank(message = "El nombre del administrador es obligatorio")
        @Size(max = 150, message = "El nombre del administrador no puede superar 150 caracteres")
        String nombreAdministrador,

        @NotBlank(message = "El email del administrador es obligatorio")
        @Email(message = "El email del administrador no es válido")
        String emailAdministrador,

        @NotBlank(message = "La contraseña del administrador es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String passwordAdministrador
) {}
