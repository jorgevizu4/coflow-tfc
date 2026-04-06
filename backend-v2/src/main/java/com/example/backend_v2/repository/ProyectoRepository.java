package com.example.backend_v2.repository;

import com.example.backend_v2.model.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    List<Proyecto> findByEmpresaId(long empresaId);
}
