package com.taskmanager.controller;

import com.taskmanager.dto.request.LoginRequestDTO;
import com.taskmanager.dto.request.EmpresaRegistroDTO;
import com.taskmanager.dto.response.ApiResponse;
import com.taskmanager.dto.response.LoginResponseDTO;
import com.taskmanager.service.AuthService;
import com.taskmanager.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final EmpresaService empresaService;

    public AuthController(AuthService authService, EmpresaService empresaService) {
        this.authService = authService;
        this.empresaService = empresaService;
    }

    /**
     * Autentica un usuario y devuelve token JWT.
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        
        LoginResponseDTO result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", result));
    }

    /**
     * Registra una empresa y su administrador inicial.
     * POST /api/v1/auth/register-company
     */
    @PostMapping("/register-company")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> registerCompany(
            @Valid @RequestBody EmpresaRegistroDTO request) {
        LoginResponseDTO result = empresaService.registrarEmpresaConAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Empresa registrada exitosamente", result));
    }
}
