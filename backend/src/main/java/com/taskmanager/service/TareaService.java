package com.taskmanager.service;

import com.taskmanager.aspect.Auditable;
import com.taskmanager.domain.entity.*;
import com.taskmanager.domain.enums.EstadoTarea;
import com.taskmanager.domain.enums.TipoComentario;
import com.taskmanager.dto.request.AsignacionDTO;
import com.taskmanager.dto.request.CambioEstadoDTO;
import com.taskmanager.dto.request.DecisionRevisionDTO;
import com.taskmanager.dto.request.MoverEstadoDTO;
import com.taskmanager.dto.request.TareaCreateDTO;
import com.taskmanager.dto.response.TareaResponseDTO;
import com.taskmanager.exception.*;
import com.taskmanager.repository.*;
import com.taskmanager.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de gestión de tareas.
 */
@Service
public class TareaService {

    private static final Logger log = LoggerFactory.getLogger(TareaService.class);

    private final TareaRepository tareaRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ComentarioRepository comentarioRepository;
    private final NotificationService notificationService;

    public TareaService(
            TareaRepository tareaRepository,
            ProyectoRepository proyectoRepository,
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            ComentarioRepository comentarioRepository,
            NotificationService notificationService) {
        this.tareaRepository = tareaRepository;
        this.proyectoRepository = proyectoRepository;
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.comentarioRepository = comentarioRepository;
        this.notificationService = notificationService;
    }

    /**
     * Crea una nueva tarea.
     */
    @Transactional
    @Auditable(accion = "CREATE", entidad = "TAREA")
    public TareaResponseDTO crear(TareaCreateDTO dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long creadorId = TenantContext.getUsuarioId();

        log.debug("Creando tarea para empresa {} por usuario {}", empresaId, creadorId);

        // Validar proyecto pertenece al tenant
        Proyecto proyecto = proyectoRepository.findByIdAndEmpresaId(dto.proyectoId(), empresaId)
                .orElseThrow(() -> new TenantAccessException("Proyecto", dto.proyectoId()));

        // Obtener empresa y creador
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa", empresaId));
        
        Usuario creador = usuarioRepository.findById(creadorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", creadorId));

        // Crear tarea
        Tarea tarea = Tarea.builder()
                .empresa(empresa)
                .proyecto(proyecto)
                .creador(creador)
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .tiempoEstimado(dto.tiempoEstimado())
                .fechaLimite(dto.fechaLimite())
                .prioridad(dto.prioridad())
                .requiereRevision(dto.requiereRevision())
                .estado(EstadoTarea.PENDIENTE)
                .build();

        tarea = tareaRepository.save(tarea);
        log.info("Tarea {} creada: '{}'", tarea.getId(), tarea.getTitulo());

        return TareaResponseDTO.fromEntity(tarea);
    }

    /**
     * Obtiene una tarea por ID (validando tenant).
     */
    @Transactional(readOnly = true)
    public TareaResponseDTO obtenerPorId(Long id) {
        Tarea tarea = obtenerTareaDelTenant(id);
        return TareaResponseDTO.fromEntity(tarea);
    }

    /**
     * Lista todas las tareas del tenant.
     */
    @Transactional(readOnly = true)
    public List<TareaResponseDTO> listarTodas() {
        Long empresaId = TenantContext.getEmpresaId();
        List<Tarea> tareas = tareaRepository.findAllByEmpresaIdAndEliminadaFalse(empresaId);
        return TareaResponseDTO.fromEntities(tareas);
    }

    /**
     * Lista tareas por proyecto.
     */
    @Transactional(readOnly = true)
    public List<TareaResponseDTO> listarPorProyecto(Long proyectoId) {
        Long empresaId = TenantContext.getEmpresaId();
        
        // Validar proyecto pertenece al tenant
        proyectoRepository.findByIdAndEmpresaId(proyectoId, empresaId)
                .orElseThrow(() -> new TenantAccessException("Proyecto", proyectoId));

        List<Tarea> tareas = tareaRepository.findAllByProyectoIdAndEliminadaFalse(proyectoId);
        return TareaResponseDTO.fromEntities(tareas);
    }

    /**
     * Lista tareas asignadas al usuario actual.
     */
    @Transactional(readOnly = true)
    public List<TareaResponseDTO> listarMisTareas() {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();
        List<Tarea> tareas = tareaRepository.findAllByEmpresaIdAndUsuarioAsignadoIdAndEliminadaFalse(empresaId, usuarioId);
        return TareaResponseDTO.fromEntities(tareas);
    }

    /**
     * Asigna una tarea a un usuario.
     * Solo LIDER o ADMIN pueden asignar.
     */
    @Transactional
    @Auditable(accion = "ASSIGN", entidad = "TAREA")
    public TareaResponseDTO asignar(Long tareaId, AsignacionDTO dto) {
        validarPermisoAsignacion();

        Long empresaId = TenantContext.getEmpresaId();
        Tarea tarea = obtenerTareaDelTenant(tareaId);

        // Validar usuario pertenece al mismo tenant
        Usuario usuarioAsignado = usuarioRepository.findByIdAndEmpresaId(dto.usuarioAsignadoId(), empresaId)
                .orElseThrow(() -> new TenantAccessException("Usuario", dto.usuarioAsignadoId()));

        // Ejecutar asignación
        tarea.asignar(usuarioAsignado, dto.prioridad(), dto.fechaLimite());
        tarea = tareaRepository.save(tarea);

        // Notificar al usuario asignado
        notificationService.notificarAsignacion(tarea, usuarioAsignado);

        log.info("Tarea {} asignada a usuario {}", tareaId, dto.usuarioAsignadoId());
        return TareaResponseDTO.fromEntity(tarea);
    }

    /**
     * Cambia el estado de la tarea (aceptar/rechazar).
     */
    @Transactional
    @Auditable(accion = "ESTADO_CHANGE", entidad = "TAREA")
    public TareaResponseDTO cambiarEstado(Long tareaId, CambioEstadoDTO dto) {
        Long usuarioId = TenantContext.getUsuarioId();
        Tarea tarea = obtenerTareaDelTenant(tareaId);

        // Validar que el usuario es el asignado
        if (tarea.getUsuarioAsignado() == null || !tarea.getUsuarioAsignado().getId().equals(usuarioId)) {
            throw new ForbiddenRoleException("cambiar estado de esta tarea", "ASIGNADO");
        }

        switch (dto.accion()) {
            case ACEPTAR -> {
                tarea.aceptar();
                log.info("Tarea {} aceptada por usuario {}", tareaId, usuarioId);
            }
            case RECHAZAR -> {
                tarea.rechazar();
                // Guardar motivo si se proporciona
                if (dto.motivo() != null && !dto.motivo().isBlank()) {
                    guardarComentario(tarea, dto.motivo(), TipoComentario.RECHAZO);
                }
                // Notificar al líder del proyecto
                notificarBloqueo(tarea);
                log.info("Tarea {} rechazada por usuario {}", tareaId, usuarioId);
            }
        }

        return TareaResponseDTO.fromEntity(tareaRepository.save(tarea));
    }

    /**
     * Lista tareas pendientes de revisión.
     * Solo LIDER, REVISOR o ADMIN.
     */
    @Transactional(readOnly = true)
    public List<TareaResponseDTO> listarPendientesRevision() {
        validarPermisoRevision();
        
        Long empresaId = TenantContext.getEmpresaId();
        List<Tarea> tareas = tareaRepository.findTareasPendientesRevision(empresaId);
        return TareaResponseDTO.fromEntities(tareas);
    }

    /**
     * Decide sobre una revisión (aprobar/rechazar).
     * Solo LIDER, REVISOR o ADMIN.
     */
    @Transactional
    @Auditable(accion = "REVIEW_DECISION", entidad = "TAREA")
    public TareaResponseDTO decidirRevision(Long tareaId, DecisionRevisionDTO dto) {
        validarPermisoRevision();

        Long usuarioId = TenantContext.getUsuarioId();
        Tarea tarea = obtenerTareaDelTenant(tareaId);

        if (tarea.getEstado() != EstadoTarea.EN_REVISION) {
            throw new InvalidStateTransitionException(tarea.getEstado().name(), "revisar");
        }

        usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new EntityNotFoundException("Usuario", usuarioId));

        if (dto.aprobado()) {
            tarea.aprobar();
            if (dto.comentario() != null && !dto.comentario().isBlank()) {
                guardarComentario(tarea, dto.comentario(), TipoComentario.APROBACION);
            }
            notificationService.notificarAprobacion(tarea);
            log.info("Tarea {} aprobada por revisor {}", tareaId, usuarioId);
        } else {
            if (dto.comentario() == null || dto.comentario().isBlank()) {
                throw new BusinessException("COMMENT_REQUIRED", "Debe proporcionar un comentario al rechazar");
            }
            tarea.rechazarRevision();
            guardarComentario(tarea, dto.comentario(), TipoComentario.RECHAZO);
            notificationService.notificarRechazoRevision(tarea, dto.comentario());
            log.info("Tarea {} rechazada en revisión por revisor {}", tareaId, usuarioId);
        }

        return TareaResponseDTO.fromEntity(tareaRepository.save(tarea));
    }

    /**
     * Mueve una tarea a un estado destino respetando transiciones del flujo.
     */
    @Transactional
    @Auditable(accion = "MOVE", entidad = "TAREA")
    public TareaResponseDTO moverEstado(Long tareaId, MoverEstadoDTO dto) {
        Tarea tarea = obtenerTareaDelTenant(tareaId);
        EstadoTarea origen = tarea.getEstado();
        EstadoTarea destino = dto.estadoDestino();

        validarPermisoMoverEstado(tarea, destino);
        validarTransicion(tarea.getEstado(), destino);

        aplicarTransicion(tarea, destino);
        tarea = tareaRepository.save(tarea);
        log.info("Tarea {} movida de {} a {} por usuario {}", tareaId, origen, destino, TenantContext.getUsuarioId());

        return TareaResponseDTO.fromEntity(tarea);
    }

    /**
     * Lista tareas con filtros dinámicos.
     */
    @Transactional(readOnly = true)
    public List<TareaResponseDTO> buscar(Specification<Tarea> spec) {
        Long empresaId = TenantContext.getEmpresaId();
        
        // Combinar con filtro de tenant
        Specification<Tarea> tenantSpec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("empresa").get("id"), empresaId),
                cb.isFalse(root.get("eliminada"))
            );
        
        List<Tarea> tareas = tareaRepository.findAll(tenantSpec.and(spec));
        return TareaResponseDTO.fromEntities(tareas);
    }

    /**
     * Elimina una tarea del tenant actual.
     * Solo LIDER o ADMIN.
     */
    @Transactional
    @Auditable(accion = "DELETE", entidad = "TAREA")
    public void eliminar(Long tareaId) {
        validarPermisoAsignacion();
        Tarea tarea = obtenerTareaDelTenant(tareaId);
        tarea.setEliminada(true);
        tarea.setEliminadaAt(LocalDateTime.now());
        tarea.setEliminadaPorUsuarioId(TenantContext.getUsuarioId());
        tareaRepository.save(tarea);
        log.info("Tarea {} eliminada por usuario {}", tareaId, TenantContext.getUsuarioId());
    }

    /**
     * Devuelve métricas de seguimiento para el dashboard de gestión.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerSeguimiento() {
        Long empresaId = TenantContext.getEmpresaId();
        List<Tarea> tareas = tareaRepository.findAllByEmpresaIdAndEliminadaFalse(empresaId);

        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoTarea estado : EstadoTarea.values()) {
            porEstado.put(estado.name(), tareas.stream().filter(t -> t.getEstado() == estado).count());
        }

        long vencidas = tareas.stream()
                .filter(t -> t.getFechaLimite() != null)
                .filter(t -> t.getFechaLimite().isBefore(LocalDateTime.now()))
                .filter(t -> t.getEstado() != EstadoTarea.COMPLETADA && t.getEstado() != EstadoTarea.APROBADA)
                .count();

        long activas = tareas.stream()
                .filter(t -> t.getEstado() != EstadoTarea.COMPLETADA && t.getEstado() != EstadoTarea.APROBADA)
                .count();

        long conRevision = tareas.stream().filter(Tarea::getRequiereRevision).count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", tareas.size());
        result.put("activas", activas);
        result.put("vencidas", vencidas);
        result.put("conRevision", conRevision);
        result.put("porEstado", porEstado);
        return result;
    }

    // ============ Métodos Privados ============

    private Tarea obtenerTareaDelTenant(Long tareaId) {
        Long empresaId = TenantContext.getEmpresaId();
        return tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(tareaId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Tarea", tareaId));
    }

    private void validarPermisoAsignacion() {
        if (!TenantContext.isLiderOrAdmin()) {
            throw new ForbiddenRoleException("asignar tareas", "LIDER o ADMIN");
        }
    }

    private void validarPermisoRevision() {
        if (!TenantContext.canReview()) {
            throw new ForbiddenRoleException("revisar tareas", "LIDER, REVISOR o ADMIN");
        }
    }

    private void guardarComentario(Tarea tarea, String contenido, TipoComentario tipo) {
        Long usuarioId = TenantContext.getUsuarioId();
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        Comentario comentario = Comentario.builder()
                .empresa(tarea.getEmpresa())
                .tarea(tarea)
                .usuario(usuario)
                .contenido(contenido)
                .tipo(tipo)
                .build();

        comentarioRepository.save(comentario);
    }

    private void notificarBloqueo(Tarea tarea) {
        if (tarea.getProyecto().getLider() != null) {
            notificationService.notificarBloqueo(tarea, tarea.getProyecto().getLider());
        }
    }

    private void validarPermisoMoverEstado(Tarea tarea, EstadoTarea destino) {
        boolean puedeGestionar = TenantContext.isLiderOrAdmin() || TenantContext.canReview();
        Long usuarioActual = TenantContext.getUsuarioId();
        boolean esAsignado = tarea.getUsuarioAsignado() != null && tarea.getUsuarioAsignado().getId().equals(usuarioActual);

        if (puedeGestionar) {
            return;
        }

        if (!esAsignado) {
            throw new ForbiddenRoleException("mover esta tarea", "ASIGNADO, LIDER, REVISOR o ADMIN");
        }

        // Usuario asignado solo puede avanzar o bloquear su trabajo.
        if (!(destino == EstadoTarea.EN_PROCESO || destino == EstadoTarea.EN_REVISION || destino == EstadoTarea.BLOQUEADA)) {
            throw new ForbiddenRoleException("mover a ese estado", "LIDER, REVISOR o ADMIN");
        }
    }

    private void validarTransicion(EstadoTarea origen, EstadoTarea destino) {
        Map<EstadoTarea, List<EstadoTarea>> transiciones = Map.of(
                EstadoTarea.PENDIENTE, List.of(EstadoTarea.ASIGNADA, EstadoTarea.EN_PROCESO),
                EstadoTarea.ASIGNADA, List.of(EstadoTarea.EN_PROCESO, EstadoTarea.BLOQUEADA),
                EstadoTarea.EN_PROCESO, List.of(EstadoTarea.EN_REVISION, EstadoTarea.COMPLETADA, EstadoTarea.BLOQUEADA),
                EstadoTarea.BLOQUEADA, List.of(EstadoTarea.EN_PROCESO, EstadoTarea.PENDIENTE),
                EstadoTarea.EN_REVISION, List.of(EstadoTarea.APROBADA, EstadoTarea.RECHAZADA),
                EstadoTarea.RECHAZADA, List.of(EstadoTarea.EN_PROCESO),
                EstadoTarea.APROBADA, List.of(EstadoTarea.COMPLETADA),
                EstadoTarea.COMPLETADA, List.of()
        );

        List<EstadoTarea> permitidos = transiciones.getOrDefault(origen, List.of());
        if (!permitidos.contains(destino)) {
            throw new InvalidStateTransitionException(origen.name(), "mover a " + destino.name());
        }
    }

    private void aplicarTransicion(Tarea tarea, EstadoTarea destino) {
        switch (destino) {
            case ASIGNADA -> tarea.setEstado(EstadoTarea.ASIGNADA);
            case EN_PROCESO -> {
                tarea.setEstado(EstadoTarea.EN_PROCESO);
                tarea.setFechaBloqueo(null);
            }
            case EN_REVISION -> tarea.setEstado(EstadoTarea.EN_REVISION);
            case APROBADA -> tarea.setEstado(EstadoTarea.APROBADA);
            case RECHAZADA -> tarea.setEstado(EstadoTarea.RECHAZADA);
            case COMPLETADA -> tarea.setEstado(EstadoTarea.COMPLETADA);
            case BLOQUEADA -> {
                tarea.setEstado(EstadoTarea.BLOQUEADA);
                tarea.setFechaBloqueo(LocalDateTime.now());
            }
            case PENDIENTE -> {
                tarea.setEstado(EstadoTarea.PENDIENTE);
                tarea.setFechaBloqueo(null);
                tarea.setUsuarioAsignado(null);
            }
        }
    }
}
