package com.example.backend_v2.repository;

import com.example.backend_v2.model.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    List<Equipo> findByEmpresaId(Long empresaId);
}
