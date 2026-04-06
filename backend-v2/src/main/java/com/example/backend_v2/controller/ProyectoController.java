package com.example.backend_v2.controller;

import com.example.backend_v2.dto.ApiResponse;
import com.example.backend_v2.dto.ProyectoCrearRequest;
import com.example.backend_v2.dto.ProyectoDTO;
import com.example.backend_v2.dto.UsuarioResumidoDTO;
import com.example.backend_v2.service.ProyectoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProyectoDTO>>> findAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", proyectoService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProyectoDTO>> findById(@PathVariable Long id) {
        return proyectoService.findById(id)
                .map(p -> ResponseEntity.ok(new ApiResponse<>(true, "OK", p)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Proyecto no encontrado", null)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProyectoDTO>> crear(@RequestBody ProyectoCrearRequest req) {
        ProyectoDTO created = proyectoService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Proyecto creado", created));
    }

    @GetMapping("/{id}/miembros")
    public ResponseEntity<ApiResponse<List<UsuarioResumidoDTO>>> getMiembros(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", proyectoService.getMiembros(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        proyectoService.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proyecto eliminado", null));
    }
}

