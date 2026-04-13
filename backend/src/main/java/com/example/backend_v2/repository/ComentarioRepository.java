package com.example.backend_v2.repository;

import com.example.backend_v2.model.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByTareaId(Long tareaId);
}
