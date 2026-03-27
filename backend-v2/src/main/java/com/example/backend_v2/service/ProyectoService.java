package com.example.backend_v2.service;

import com.example.backend_v2.model.entity.Proyecto;
import com.example.backend_v2.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final AuthService authService;

    public List<Proyecto> findAll() {
        return proyectoRepository.findByEmpresaId(authService.getEmpresaIdActual());
    }

    public Optional<Proyecto> findById(Long id) {
        return proyectoRepository.findById(id)
                .filter(p -> p.getEmpresa().getId() == authService.getEmpresaIdActual());
    }

    public Proyecto save(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    public void deleteById(Long id) {
        proyectoRepository.deleteById(id);
    }
}
