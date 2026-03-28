package com.example.backend_v2.service;

import com.example.backend_v2.dto.*;
import com.example.backend_v2.model.entity.Proyecto;
import com.example.backend_v2.model.entity.Tarea;
import com.example.backend_v2.model.entity.Usuario;
import com.example.backend_v2.model.enums.EstadoTarea;
import com.example.backend_v2.repository.ProyectoRepository;
import com.example.backend_v2.repository.TareaRepository;
import com.example.backend_v2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    public List<TareaDTO> findAll() {
        return tareaRepository.findByEmpresaId(authService.getEmpresaIdActual())
                .stream().map(TareaDTO::from).collect(Collectors.toList());
    }

    public List<TareaDTO> findByProyectoId(Long proyectoId) {
        return tareaRepository.findByProyectoId(proyectoId)
                .stream().map(TareaDTO::from).collect(Collectors.toList());
    }

    public Optional<TareaDTO> findById(Long id) {
        Long empresaId = authService.getEmpresaIdActual();
        return tareaRepository.findById(id)
                .filter(t -> t.getProyecto() != null
                        && t.getProyecto().getEmpresa() != null
                        && t.getProyecto().getEmpresa().getId().equals(empresaId))
                .map(TareaDTO::from);
    }

    public List<TareaDTO> misTareas() {
        Long empresaId = authService.getEmpresaIdActual();
        Long usuarioId = authService.getUsuarioActual().getId();
        return tareaRepository.findMisTareas(empresaId, usuarioId)
                .stream().map(TareaDTO::from).collect(Collectors.toList());
    }

    public List<TareaDTO> pendientesRevision() {
        return tareaRepository.findByEmpresaIdAndEstado(authService.getEmpresaIdActual(), EstadoTarea.EN_REVISION)
                .stream().map(TareaDTO::from).collect(Collectors.toList());
    }

    public Map<String, Long> getSeguimiento() {
        List<Tarea> tareas = tareaRepository.findByEmpresaId(authService.getEmpresaIdActual());
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (EstadoTarea estado : EstadoTarea.values()) {
            conteo.put(estado.name(), 0L);
        }
        tareas.forEach(t -> {
            String key = t.getEstadoTarea() != null ? t.getEstadoTarea().name() : "SIN_ESTADO";
            conteo.merge(key, 1L, Long::sum);
        });
        return conteo;
    }

    public TareaDTO crear(TareaCrearRequest req) {
        Long empresaId = authService.getEmpresaIdActual();
        Proyecto proyecto = proyectoRepository.findById(req.getProyectoId())
                .filter(p -> p.getEmpresa() != null && p.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));

        Usuario creador = authService.getUsuarioActual();
        String ahora = LocalDateTime.now().toString();

        Tarea tarea = new Tarea();
        tarea.setTitulo(req.getTitulo());
        tarea.setNombre(req.getTitulo());
        tarea.setDescripcion(req.getDescripcion());
        tarea.setProyecto(proyecto);
        tarea.setCreadoPor(creador);
        tarea.setEstadoTarea(EstadoTarea.PENDIENTE);
        tarea.setPrioridad(req.getPrioridad());
        tarea.setRequiereRevision(Boolean.TRUE.equals(req.getRequiereRevision()));
        tarea.setTiempoEstimado(req.getTiempoEstimado());
        tarea.setFechaLimite(req.getFechaLimite());
        tarea.setCreatedAt(ahora);
        tarea.setUpdatedAt(ahora);

        if (req.getUsuarioAsignadoId() != null) {
            Usuario asignado = usuarioRepository.findById(req.getUsuarioAsignadoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
            tarea.setUsuarioAsignado(asignado);
            tarea.setEstadoTarea(EstadoTarea.ASIGNADA);
        }
        return TareaDTO.from(tareaRepository.save(tarea));
    }

    public TareaDTO asignar(Long tareaId, AsignacionRequest req) {
        Long empresaId = authService.getEmpresaIdActual();
        Tarea tarea = tareaRepository.findById(tareaId)
                .filter(t -> t.getProyecto() != null
                        && t.getProyecto().getEmpresa() != null
                        && t.getProyecto().getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        Usuario asignado = usuarioRepository.findById(req.getUsuarioAsignadoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        tarea.setUsuarioAsignado(asignado);
        tarea.setEstadoTarea(EstadoTarea.ASIGNADA);
        if (req.getPrioridad() != null) tarea.setPrioridad(req.getPrioridad());
        if (req.getFechaLimite() != null) tarea.setFechaLimite(req.getFechaLimite());
        tarea.setUpdatedAt(LocalDateTime.now().toString());

        return TareaDTO.from(tareaRepository.save(tarea));
    }

    public TareaDTO cambiarEstado(Long tareaId, CambioEstadoRequest req) {
        Long empresaId = authService.getEmpresaIdActual();
        Tarea tarea = tareaRepository.findById(tareaId)
                .filter(t -> t.getProyecto() != null
                        && t.getProyecto().getEmpresa() != null
                        && t.getProyecto().getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        if ("ACEPTAR".equalsIgnoreCase(req.getAccion())) {
            tarea.setEstadoTarea(EstadoTarea.EN_PROCESO);
        } else if ("RECHAZAR".equalsIgnoreCase(req.getAccion())) {
            tarea.setEstadoTarea(EstadoTarea.PENDIENTE);
            tarea.setUsuarioAsignado(null);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Acción debe ser ACEPTAR o RECHAZAR");
        }
        tarea.setUpdatedAt(LocalDateTime.now().toString());
        return TareaDTO.from(tareaRepository.save(tarea));
    }

    public TareaDTO moverEstado(Long tareaId, MoverEstadoRequest req) {
        Long empresaId = authService.getEmpresaIdActual();
        Tarea tarea = tareaRepository.findById(tareaId)
                .filter(t -> t.getProyecto() != null
                        && t.getProyecto().getEmpresa() != null
                        && t.getProyecto().getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        tarea.setEstadoTarea(req.getEstadoDestino());
        tarea.setUpdatedAt(LocalDateTime.now().toString());
        return TareaDTO.from(tareaRepository.save(tarea));
    }

    public TareaDTO decidirRevision(Long tareaId, DecisionRevisionRequest req) {
        Long empresaId = authService.getEmpresaIdActual();
        Tarea tarea = tareaRepository.findById(tareaId)
                .filter(t -> t.getProyecto() != null
                        && t.getProyecto().getEmpresa() != null
                        && t.getProyecto().getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        tarea.setEstadoTarea(Boolean.TRUE.equals(req.getAprobado()) ? EstadoTarea.APROBADA : EstadoTarea.RECHAZADA);
        tarea.setUpdatedAt(LocalDateTime.now().toString());
        return TareaDTO.from(tareaRepository.save(tarea));
    }

    public void deleteById(Long id) {
        Long empresaId = authService.getEmpresaIdActual();
        Tarea tarea = tareaRepository.findById(id)
                .filter(t -> t.getProyecto() != null
                        && t.getProyecto().getEmpresa() != null
                        && t.getProyecto().getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
        tareaRepository.deleteById(tarea.getId());
    }
}
