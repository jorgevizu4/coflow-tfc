package com.taskmanager.controller;

import com.taskmanager.dto.request.UsuarioCreateDTO;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.dto.response.UsuarioResponseDTO;
import com.taskmanager.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de usuarios por empresa.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponseDTO>>> listar() {
        return ResponseEntity.ok(ApiResponse.success(usuarioService.listarEmpresaActual()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> crear(@Valid @RequestBody UsuarioCreateDTO dto) {
        UsuarioResponseDTO creado = usuarioService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario creado exitosamente", creado));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean activo = body.getOrDefault("activo", true);
        UsuarioResponseDTO actualizado = usuarioService.cambiarEstado(id, activo);
        return ResponseEntity.ok(ApiResponse.success("Estado de usuario actualizado", actualizado));
    }
}
