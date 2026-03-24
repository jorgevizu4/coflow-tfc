package com.taskmanager.service;

import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.dto.request.LoginRequestDTO;
import com.taskmanager.dto.response.LoginResponseDTO;
import com.taskmanager.repository.UsuarioRepository;
import com.taskmanager.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de autenticación.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Autentica un usuario y genera token JWT.
     */
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        log.debug("Intentando login para: {}", request.email());

        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        // Verificar contraseña
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            log.warn("Contraseña incorrecta para usuario: {}", request.email());
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // Actualizar último login
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        // Generar token JWT
        String token = jwtService.generateToken(
                usuario.getId(),
                usuario.getEmpresa().getId(),
                usuario.getRol().name(),
                usuario.getEmail()
        );

        log.info("Login exitoso para usuario: {} (empresa: {})", 
                usuario.getEmail(), usuario.getEmpresa().getNombre());

        return LoginResponseDTO.of(token, usuario);
    }

}
