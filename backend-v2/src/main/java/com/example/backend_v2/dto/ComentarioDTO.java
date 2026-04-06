package com.example.backend_v2.dto;

import com.example.backend_v2.model.entity.Comentario;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ComentarioDTO {
    private final Long id;
    private final String contenido;
    private final LocalDateTime fechaCreacion;
    private final Long autorId;
    private final String autorNombre;

    public ComentarioDTO(Comentario c) {
        this.id = c.getId();
        this.contenido = c.getContenido();
        this.fechaCreacion = c.getFechaCreacion();
        this.autorId = c.getUsuario() != null ? c.getUsuario().getId() : null;
        if (c.getUsuario() != null) {
            String n = (c.getUsuario().getNombre() + " " + c.getUsuario().getApellidos()).trim();
            this.autorNombre = n.isEmpty() ? c.getUsuario().getEmail() : n;
        } else {
            this.autorNombre = "Desconocido";
        }
    }
}
