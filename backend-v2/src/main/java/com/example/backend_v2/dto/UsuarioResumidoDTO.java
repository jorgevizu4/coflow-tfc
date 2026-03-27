package com.example.backend_v2.dto;

import com.example.backend_v2.model.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResumidoDTO {
    private Long id;
    private String nombreCompleto;
    private String email;

    public static UsuarioResumidoDTO from(Usuario u) {
        if (u == null) return null;
        String nombre = u.getNombre() != null ? u.getNombre() : "";
        String apellidos = u.getApellidos() != null ? " " + u.getApellidos() : "";
        return UsuarioResumidoDTO.builder()
                .id(u.getId())
                .nombreCompleto((nombre + apellidos).trim())
                .email(u.getEmail())
                .build();
    }
}
