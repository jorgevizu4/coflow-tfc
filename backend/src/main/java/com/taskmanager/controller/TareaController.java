package com.taskmanager.controller;

import com.taskmanager.dto.request.AsignacionDTO;
import com.taskmanager.dto.request.CambioEstadoDTO;
import com.taskmanager.dto.request.DecisionRevisionDTO;
import com.taskmanager.dto.request.MoverEstadoDTO;
import com.taskmanager.dto.request.TareaCreateDTO;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.dto.response.TareaResponseDTO;
import com.taskmanager.domain.entity.Tarea;
import com.taskmanager.domain.enums.EstadoTarea;
import com.taskmanager.domain.enums.Prioridad;
import com.taskmanager.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de tareas.
 */
@RestController
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    /**
     * Crea una nueva tarea.
     * POST /api/v1/tareas
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TareaResponseDTO>> crear(
            @Valid @RequestBody TareaCreateDTO dto) {
        
        TareaResponseDTO tarea = tareaService.crear(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tarea creada exitosamente", tarea));
    }

    /**
     * Obtiene una tarea por ID.
     * GET /api/v1/tareas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TareaResponseDTO>> obtenerPorId(@PathVariable Long id) {
        TareaResponseDTO tarea = tareaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(tarea));
    }

    /**
     * Lista todas las tareas del tenant.
     * GET /api/v1/tareas
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TareaResponseDTO>>> listarTodas(
            @RequestParam(required = false) EstadoTarea estado,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) Long usuarioAsignadoId) {

        Specification<Tarea> spec = Specification.where(null);

        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        if (prioridad != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("prioridad"), prioridad));
        }
        if (proyectoId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("proyecto").get("id"), proyectoId));
        }
        if (usuarioAsignadoId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("usuarioAsignado").get("id"), usuarioAsignadoId));
        }

        List<TareaResponseDTO> tareas = tareaService.buscar(spec);
        return ResponseEntity.ok(ApiResponse.success(tareas));
    }

    /**
     * Lista tareas de un proyecto específico.
     * GET /api/v1/tareas/proyecto/{proyectoId}
     */
    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<ApiResponse<List<TareaResponseDTO>>> listarPorProyecto(
            @PathVariable Long proyectoId) {
        
        List<TareaResponseDTO> tareas = tareaService.listarPorProyecto(proyectoId);
        return ResponseEntity.ok(ApiResponse.success(tareas));
    }

    /**
     * Lista tareas asignadas al usuario actual.
     * GET /api/v1/tareas/mis-tareas
     */
    @GetMapping("/mis-tareas")
    public ResponseEntity<ApiResponse<List<TareaResponseDTO>>> listarMisTareas() {
        List<TareaResponseDTO> tareas = tareaService.listarMisTareas();
        return ResponseEntity.ok(ApiResponse.success(tareas));
    }

    /**
     * Asigna una tarea a un usuario.
     * PATCH /api/v1/tareas/{id}/asignar
     * Solo LIDER o ADMIN.
     */
    @PatchMapping("/{id}/asignar")
    public ResponseEntity<ApiResponse<TareaResponseDTO>> asignar(
            @PathVariable Long id,
            @Valid @RequestBody AsignacionDTO dto) {
        
        TareaResponseDTO tarea = tareaService.asignar(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Tarea asignada exitosamente", tarea));
    }

    /**
     * Cambia el estado de una tarea (aceptar/rechazar).
     * PATCH /api/v1/tareas/{id}/estado
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<TareaResponseDTO>> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoDTO dto) {
        
        TareaResponseDTO tarea = tareaService.cambiarEstado(id, dto);
        String mensaje = dto.accion() == CambioEstadoDTO.AccionEstado.ACEPTAR 
                ? "Tarea aceptada" 
                : "Tarea rechazada";
        return ResponseEntity.ok(ApiResponse.success(mensaje, tarea));
    }

    /**
     * Lista tareas pendientes de revisión.
     * GET /api/v1/tareas/pendientes-revision
     * Solo LIDER, REVISOR o ADMIN.
     */
    @GetMapping("/pendientes-revision")
    public ResponseEntity<ApiResponse<List<TareaResponseDTO>>> listarPendientesRevision() {
        List<TareaResponseDTO> tareas = tareaService.listarPendientesRevision();
        return ResponseEntity.ok(ApiResponse.success(tareas));
    }

    /**
     * Decide sobre una revisión (aprobar/rechazar).
     * POST /api/v1/tareas/{id}/decision
     * Solo LIDER, REVISOR o ADMIN.
     */
    @PostMapping("/{id}/decision")
    public ResponseEntity<ApiResponse<TareaResponseDTO>> decidirRevision(
            @PathVariable Long id,
            @Valid @RequestBody DecisionRevisionDTO dto) {
        
        TareaResponseDTO tarea = tareaService.decidirRevision(id, dto);
        String mensaje = dto.aprobado() ? "Tarea aprobada" : "Tarea rechazada en revisión";
        return ResponseEntity.ok(ApiResponse.success(mensaje, tarea));
    }

    /**
     * Mueve una tarea entre estados para el tablero dinámico.
     * PATCH /api/v1/tareas/{id}/mover
     */
    @PatchMapping("/{id}/mover")
    public ResponseEntity<ApiResponse<TareaResponseDTO>> mover(
            @PathVariable Long id,
            @Valid @RequestBody MoverEstadoDTO dto) {
        TareaResponseDTO tarea = tareaService.moverEstado(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Tarea actualizada", tarea));
    }

    /**
     * Elimina una tarea.
     * DELETE /api/v1/tareas/{id}
     * Solo LIDER o ADMIN.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        tareaService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success("Tarea eliminada exitosamente", null));
    }

    /**
     * Obtiene métricas de seguimiento para todo el ciclo de vida.
     * GET /api/v1/tareas/seguimiento
     */
    @GetMapping("/seguimiento")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seguimiento() {
        Map<String, Object> data = tareaService.obtenerSeguimiento();
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
