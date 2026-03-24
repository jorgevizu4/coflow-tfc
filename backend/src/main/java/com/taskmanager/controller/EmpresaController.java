package com.taskmanager.controller;

import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.dto.response.EmpresaResponseDTO;
import com.taskmanager.service.EmpresaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para operaciones del tenant actual.
 */
@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<EmpresaResponseDTO>> obtenerEmpresaActual() {
        return ResponseEntity.ok(ApiResponse.success(empresaService.obtenerEmpresaActual()));
    }
}
