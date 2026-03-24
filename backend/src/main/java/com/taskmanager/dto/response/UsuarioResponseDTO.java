package com.taskmanager.dto.response;

import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.domain.enums.RolUsuario;

/**
 * DTO de salida para usuarios.
 */
public record UsuarioResponseDTO(
        Long id,
        String nombreCompleto,
        String email,
        RolUsuario rol,
        Boolean activo,
        Long empresaId,
        String empresaNombre
) {
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getActivo(),
                usuario.getEmpresa().getId(),
                usuario.getEmpresa().getNombre()
        );
    }
}
