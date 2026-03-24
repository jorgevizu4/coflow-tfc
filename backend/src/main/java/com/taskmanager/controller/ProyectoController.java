package com.taskmanager.controller;

import com.taskmanager.dto.request.ProyectoCreateDTO;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.dto.response.ProyectoResponseDTO;
import com.taskmanager.service.ProyectoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de proyectos.
 */
@RestController
@RequestMapping("/proyectos")
public class ProyectoController {

    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProyectoResponseDTO>>> listar() {
        return ResponseEntity.ok(ApiResponse.success(proyectoService.listarEmpresaActual()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProyectoResponseDTO>> crear(@Valid @RequestBody ProyectoCreateDTO dto) {
        ProyectoResponseDTO creado = proyectoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Proyecto creado exitosamente", creado));
    }
}
