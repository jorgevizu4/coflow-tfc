package com.example.backend_v2.service;

import com.example.backend_v2.model.entity.Tarea;
import com.example.backend_v2.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;
    private final AuthService authService;

    public List<Tarea> findAll() {
        return tareaRepository.findByEmpresaId(authService.getEmpresaIdActual());
    }

    public List<Tarea> findByProyectoId(Long proyectoId) {
        return tareaRepository.findByProyectoId(proyectoId);
    }

    public Optional<Tarea> findById(Long id) {
        return tareaRepository.findById(id)
                .filter(t -> t.getProyecto().getEmpresa().getId() == authService.getEmpresaIdActual());
    }

    public Tarea save(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    public void deleteById(Long id) {
        tareaRepository.deleteById(id);
    }
}
