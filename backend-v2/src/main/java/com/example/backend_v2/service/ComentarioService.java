package com.example.backend_v2.service;

import com.example.backend_v2.model.entity.Comentario;
import com.example.backend_v2.repository.ComentarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;

    public List<Comentario> findByTareaId(Long tareaId) {
        return comentarioRepository.findByTareaId(tareaId);
    }

    public Optional<Comentario> findById(Long id) {
        return comentarioRepository.findById(id);
    }

    public Comentario save(Comentario comentario) {
        return comentarioRepository.save(comentario);
    }

    public void deleteById(Long id) {
        comentarioRepository.deleteById(id);
    }
}
