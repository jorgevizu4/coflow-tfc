package com.taskmanager.dto.response;

import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.domain.enums.RolUsuario;

/**
 * DTO de respuesta con token JWT y datos del usuario.
 */
public record LoginResponseDTO(
        String token,
        String tipo,
        Long usuarioId,
        String nombreCompleto,
        String email,
        RolUsuario rol,
        Long empresaId,
        String empresaNombre
) {
    public static LoginResponseDTO of(String token, Usuario usuario) {
        return new LoginResponseDTO(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getEmpresa().getId(),
                usuario.getEmpresa().getNombre()
        );
    }
}
