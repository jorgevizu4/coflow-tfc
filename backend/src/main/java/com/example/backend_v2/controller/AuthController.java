package com.example.backend_v2.controller;

import com.example.backend_v2.dto.ApiResponse;
import com.example.backend_v2.dto.AuthRequest;
import com.example.backend_v2.dto.AuthResponse;
import com.example.backend_v2.dto.RegisterCompanyRequest;
import com.example.backend_v2.dto.RegisterRequest;
import com.example.backend_v2.model.entity.Empresa;
import com.example.backend_v2.model.entity.Usuario;
import com.example.backend_v2.model.enums.RolUsuario;
import com.example.backend_v2.repository.EmpresaRepository;
import com.example.backend_v2.repository.UsuarioRepository;
import com.example.backend_v2.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.<AuthResponse>builder()
                            .success(false)
                            .message("Credenciales incorrectas")
                            .build()
            );
        }
        Usuario usuario = (Usuario) userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(usuario);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login exitoso")
                .data(buildAuthResponse(token, usuario))
                .build());
    }

    @PostMapping("/register-company")
    public ResponseEntity<ApiResponse<AuthResponse>> registerCompany(@RequestBody RegisterCompanyRequest request) {
        Empresa empresa = new Empresa();
        empresa.setNombre(request.getNombreEmpresa());
        empresa.setFechaCreacion(LocalDate.now());
        empresa = empresaRepository.save(empresa);

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombreAdministrador());
        usuario.setEmail(request.getEmailAdministrador());
        usuario.setPassword(passwordEncoder.encode(request.getPasswordAdministrador()));
        usuario.setRol(RolUsuario.ADMIN);
        usuario.setFechaCreacion(LocalDate.now());
        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Empresa registrada correctamente")
                .data(buildAuthResponse(token, usuario))
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ApiResponse.<AuthResponse>builder()
                            .success(false).message("El email ya está registrado").build());
        }
        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElse(null);
        if (empresa == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<AuthResponse>builder()
                            .success(false).message("Empresa no encontrada").build());
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellidos(request.getApellidos());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(RolUsuario.USUARIO);
        usuario.setFechaCreacion(LocalDate.now());
        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Usuario registrado correctamente")
                .data(buildAuthResponse(token, usuario))
                .build());
    }

    private AuthResponse buildAuthResponse(String token, Usuario usuario) {
        String nombreCompleto = usuario.getNombre();
        if (usuario.getApellidos() != null && !usuario.getApellidos().isBlank()) {
            nombreCompleto += " " + usuario.getApellidos();
        }
        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .nombreCompleto(nombreCompleto)
                .email(usuario.getEmail())
                .rol(usuario.getRol() != null ? usuario.getRol().name() : "USUARIO")
                .empresaId(usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null)
                .empresaNombre(usuario.getEmpresa() != null ? usuario.getEmpresa().getNombre() : null)
                .build();
    }
}

