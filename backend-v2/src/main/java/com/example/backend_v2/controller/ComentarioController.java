package com.example.backend_v2.controller;

import com.example.backend_v2.model.entity.Comentario;
import com.example.backend_v2.service.ComentarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping("/tarea/{tareaId}")
    public List<Comentario> findByTareaId(@PathVariable Long tareaId) {
        return comentarioService.findByTareaId(tareaId);
    }

    @PostMapping
    public Comentario save(@RequestBody Comentario comentario) {
        return comentarioService.save(comentario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        comentarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
