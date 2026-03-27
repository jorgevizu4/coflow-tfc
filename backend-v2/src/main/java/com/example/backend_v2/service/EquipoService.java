package com.example.backend_v2.service;

import com.example.backend_v2.model.entity.Equipo;
import com.example.backend_v2.repository.EquipoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final AuthService authService;

    public List<Equipo> findAll() {
        return equipoRepository.findByEmpresaId(authService.getEmpresaIdActual());
    }

    public Optional<Equipo> findById(Long id) {
        return equipoRepository.findById(id)
                .filter(e -> e.getEmpresa().getId() == authService.getEmpresaIdActual());
    }

    public Equipo save(Equipo equipo) {
        return equipoRepository.save(equipo);
    }

    public void deleteById(Long id) {
        equipoRepository.deleteById(id);
    }
}
