package com.example.backend_v2.service;

import com.example.backend_v2.dto.ComentarioCrearRequest;
import com.example.backend_v2.dto.ComentarioDTO;
import com.example.backend_v2.model.entity.Comentario;
import com.example.backend_v2.model.entity.Tarea;
import com.example.backend_v2.model.entity.Usuario;
import com.example.backend_v2.repository.ComentarioRepository;
import com.example.backend_v2.repository.TareaRepository;
import com.example.backend_v2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final TareaRepository tareaRepository;
    private final UsuarioRepository usuarioRepository;

    public List<ComentarioDTO> findByTareaId(Long tareaId) {
        return comentarioRepository.findByTareaId(tareaId)
                .stream().map(ComentarioDTO::new).toList();
    }

    public ComentarioDTO crear(ComentarioCrearRequest req, String email) {
        if (req.getContenido() == null || req.getContenido().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El contenido no puede estar vacío");
        }
        Tarea tarea = tareaRepository.findById(req.getTareaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Comentario c = new Comentario();
        c.setContenido(req.getContenido().trim());
        c.setFechaCreacion(LocalDateTime.now());
        c.setTarea(tarea);
        c.setUsuario(usuario);
        return new ComentarioDTO(comentarioRepository.save(c));
    }

    public void deleteById(Long id, String email) {
        Comentario c = comentarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));
        if (!c.getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar este comentario");
        }
        comentarioRepository.deleteById(id);
    }
}
