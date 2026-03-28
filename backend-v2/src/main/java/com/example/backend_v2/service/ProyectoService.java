package com.example.backend_v2.service;

import com.example.backend_v2.dto.ProyectoCrearRequest;
import com.example.backend_v2.dto.ProyectoDTO;
import com.example.backend_v2.dto.UsuarioResumidoDTO;
import com.example.backend_v2.model.entity.Empresa;
import com.example.backend_v2.model.entity.Proyecto;
import com.example.backend_v2.model.entity.Usuario;
import com.example.backend_v2.repository.EmpresaRepository;
import com.example.backend_v2.repository.ProyectoRepository;
import com.example.backend_v2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final AuthService authService;

    public List<ProyectoDTO> findAll() {
        return proyectoRepository.findByEmpresaId(authService.getEmpresaIdActual())
                .stream().map(ProyectoDTO::from).collect(Collectors.toList());
    }

    public List<UsuarioResumidoDTO> getMiembros(Long proyectoId) {
        Long empresaId = authService.getEmpresaIdActual();
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .filter(p -> p.getEmpresa() != null && p.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));

        List<Usuario> miembros = (proyecto.getUsuarios() != null && !proyecto.getUsuarios().isEmpty())
                ? proyecto.getUsuarios()
                : usuarioRepository.findByEmpresaId(empresaId);

        return miembros.stream().map(UsuarioResumidoDTO::from).collect(Collectors.toList());
    }

    public Optional<ProyectoDTO> findById(Long id) {
        Long empresaId = authService.getEmpresaIdActual();
        return proyectoRepository.findById(id)
                .filter(p -> p.getEmpresa() != null && p.getEmpresa().getId().equals(empresaId))
                .map(ProyectoDTO::from);
    }

    public ProyectoDTO crear(ProyectoCrearRequest req) {
        Long empresaId = authService.getEmpresaIdActual();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));

        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(req.getTitulo());
        proyecto.setTitulo(req.getTitulo());
        proyecto.setDescripcion(req.getDescripcion());
        proyecto.setFechaFinEstimada(req.getFechaFinEstimada());
        proyecto.setEmpresa(empresa);

        if (req.getLiderId() != null) {
            Usuario lider = usuarioRepository.findById(req.getLiderId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lider no encontrado"));
            proyecto.setLider(lider);
        }

        return ProyectoDTO.from(proyectoRepository.save(proyecto));
    }

    public void deleteById(Long id) {
        Long empresaId = authService.getEmpresaIdActual();
        proyectoRepository.findById(id)
                .filter(p -> p.getEmpresa() != null && p.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
        proyectoRepository.deleteById(id);
    }
}
