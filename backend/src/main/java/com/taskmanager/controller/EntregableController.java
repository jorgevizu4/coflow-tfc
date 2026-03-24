package com.taskmanager.controller;

import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.dto.response.EntregableResponseDTO;
import com.taskmanager.service.EntregableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para gestión de entregables.
 */
@RestController
@RequestMapping("/tareas/{tareaId}/entregables")
public class EntregableController {

    private final EntregableService entregableService;

    public EntregableController(EntregableService entregableService) {
        this.entregableService = entregableService;
    }

    /**
     * Sube un entregable para una tarea.
     * POST /api/v1/tareas/{tareaId}/entregables
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EntregableResponseDTO>> subirEntregable(
            @PathVariable Long tareaId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "comentarios", required = false) String comentarios) {

        EntregableResponseDTO entregable = entregableService.subirEntregable(tareaId, archivo, comentarios);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Entregable subido exitosamente", entregable));
    }

    /**
     * Lista entregables de una tarea.
     * GET /api/v1/tareas/{tareaId}/entregables
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EntregableResponseDTO>>> listarEntregables(
            @PathVariable Long tareaId) {

        List<EntregableResponseDTO> entregables = entregableService.listarPorTarea(tareaId);
        return ResponseEntity.ok(ApiResponse.success(entregables));
    }

    /**
     * Obtiene un entregable específico.
     * GET /api/v1/tareas/{tareaId}/entregables/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EntregableResponseDTO>> obtenerEntregable(
            @PathVariable Long tareaId,
            @PathVariable Long id) {

        EntregableResponseDTO entregable = entregableService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(entregable));
    }
}
