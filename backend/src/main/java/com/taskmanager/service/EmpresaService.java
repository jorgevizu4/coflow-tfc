package com.taskmanager.service;

import com.taskmanager.domain.entity.Empresa;
import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.domain.enums.RolUsuario;
import com.taskmanager.dto.request.EmpresaRegistroDTO;
import com.taskmanager.dto.response.EmpresaResponseDTO;
import com.taskmanager.dto.response.LoginResponseDTO;
import com.taskmanager.exception.BusinessException;
import com.taskmanager.exception.EntityNotFoundException;
import com.taskmanager.repository.EmpresaRepository;
import com.taskmanager.repository.UsuarioRepository;
import com.taskmanager.security.JwtService;
import com.taskmanager.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de gestión de empresa/tenant.
 */
@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponseDTO registrarEmpresaConAdmin(EmpresaRegistroDTO dto) {
        String emailAdmin = dto.emailAdministrador().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailAdmin)) {
            throw new BusinessException("EMAIL_EXISTS", "El email del administrador ya está en uso");
        }

        Empresa empresa = Empresa.builder()
                .nombre(dto.nombreEmpresa().trim())
                .build();
        empresa = empresaRepository.save(empresa);

        Usuario admin = Usuario.builder()
                .empresa(empresa)
                .nombreCompleto(dto.nombreAdministrador().trim())
                .email(emailAdmin)
                .passwordHash(passwordEncoder.encode(dto.passwordAdministrador()))
                .rol(RolUsuario.ADMIN)
                .activo(true)
                .build();
        admin = usuarioRepository.save(admin);

        String token = jwtService.generateToken(
                admin.getId(),
                empresa.getId(),
                admin.getRol().name(),
                admin.getEmail()
        );

        return LoginResponseDTO.of(token, admin);
    }

    @Transactional(readOnly = true)
    public EmpresaResponseDTO obtenerEmpresaActual() {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa", empresaId));
        return EmpresaResponseDTO.fromEntity(empresa);
    }
}
