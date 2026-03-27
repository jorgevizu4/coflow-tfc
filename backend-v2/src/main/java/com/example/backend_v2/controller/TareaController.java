package com.example.backend_v2.controller;

import com.example.backend_v2.dto.*;
import com.example.backend_v2.service.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tareas")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TareaDTO>>> findAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", tareaService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TareaDTO>> findById(@PathVariable Long id) {
        return tareaService.findById(id)
                .map(t -> ResponseEntity.ok(new ApiResponse<>(true, "OK", t)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Tarea no encontrada", null)));
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<ApiResponse<List<TareaDTO>>> findByProyecto(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", tareaService.findByProyectoId(proyectoId)));
    }

    @GetMapping("/mis-tareas")
    public ResponseEntity<ApiResponse<List<TareaDTO>>> misTareas() {
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", tareaService.misTareas()));
    }

    @GetMapping("/pendientes-revision")
    public ResponseEntity<ApiResponse<List<TareaDTO>>> pendientesRevision() {
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", tareaService.pendientesRevision()));
    }

    @GetMapping("/seguimiento")
    public ResponseEntity<ApiResponse<Map<String, Long>>> seguimiento() {
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", tareaService.getSeguimiento()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TareaDTO>> crear(@RequestBody TareaCrearRequest req) {
        TareaDTO created = tareaService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Tarea creada", created));
    }

    @PatchMapping("/{id}/asignar")
    public ResponseEntity<ApiResponse<TareaDTO>> asignar(@PathVariable Long id,
                                                          @RequestBody AsignacionRequest req) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tarea asignada", tareaService.asignar(id, req)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<TareaDTO>> cambiarEstado(@PathVariable Long id,
                                                                @RequestBody CambioEstadoRequest req) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Estado actualizado", tareaService.cambiarEstado(id, req)));
    }

    @PatchMapping("/{id}/mover")
    public ResponseEntity<ApiResponse<TareaDTO>> moverEstado(@PathVariable Long id,
                                                              @RequestBody MoverEstadoRequest req) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Estado movido", tareaService.moverEstado(id, req)));
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<ApiResponse<TareaDTO>> decidirRevision(@PathVariable Long id,
                                                                   @RequestBody DecisionRevisionRequest req) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Decisión registrada", tareaService.decidirRevision(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        tareaService.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tarea eliminada", null));
    }
}

