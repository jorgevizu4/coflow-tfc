package com.taskmanager.service;

import com.taskmanager.aspect.Auditable;
import com.taskmanager.domain.entity.Empresa;
import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.domain.enums.RolUsuario;
import com.taskmanager.dto.request.UsuarioCreateDTO;
import com.taskmanager.dto.response.UsuarioResponseDTO;
import com.taskmanager.exception.BusinessException;
import com.taskmanager.exception.EntityNotFoundException;
import com.taskmanager.exception.ForbiddenRoleException;
import com.taskmanager.repository.EmpresaRepository;
import com.taskmanager.repository.UsuarioRepository;
import com.taskmanager.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de administración de usuarios por empresa.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarEmpresaActual() {
        Long empresaId = TenantContext.getEmpresaId();
        return usuarioRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(UsuarioResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    @Auditable(accion = "CREATE", entidad = "USUARIO")
    public UsuarioResponseDTO crear(UsuarioCreateDTO dto) {
        validarPermisoGestionUsuarios();
        Long empresaId = TenantContext.getEmpresaId();

        if (usuarioRepository.existsByEmpresaIdAndEmail(empresaId, dto.email())) {
            throw new BusinessException("USER_EMAIL_EXISTS", "Ya existe un usuario con ese email en la empresa");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa", empresaId));

        Usuario usuario = Usuario.builder()
                .empresa(empresa)
                .nombreCompleto(dto.nombreCompleto())
                .email(dto.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .rol(dto.rol())
                .activo(true)
                .build();

        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    @Auditable(accion = "DEACTIVATE", entidad = "USUARIO")
    public UsuarioResponseDTO cambiarEstado(Long usuarioId, boolean activo) {
        validarPermisoGestionUsuarios();
        Long empresaId = TenantContext.getEmpresaId();

        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", usuarioId));

        Long usuarioActual = TenantContext.getUsuarioId();
        if (usuario.getId().equals(usuarioActual) && !activo) {
            throw new BusinessException("CANNOT_DEACTIVATE_SELF", "No puedes desactivar tu propio usuario");
        }

        usuario.setActivo(activo);
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    private void validarPermisoGestionUsuarios() {
        RolUsuario rol = TenantContext.getRol();
        if (rol != RolUsuario.ADMIN && rol != RolUsuario.LIDER) {
            throw new ForbiddenRoleException("gestionar usuarios", "ADMIN o LIDER");
        }
    }
}
