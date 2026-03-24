package com.taskmanager.service;

import com.taskmanager.aspect.Auditable;
import com.taskmanager.domain.entity.Empresa;
import com.taskmanager.domain.entity.Proyecto;
import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.domain.enums.RolUsuario;
import com.taskmanager.dto.request.ProyectoCreateDTO;
import com.taskmanager.dto.response.ProyectoResponseDTO;
import com.taskmanager.exception.EntityNotFoundException;
import com.taskmanager.exception.ForbiddenRoleException;
import com.taskmanager.exception.TenantAccessException;
import com.taskmanager.repository.EmpresaRepository;
import com.taskmanager.repository.ProyectoRepository;
import com.taskmanager.repository.UsuarioRepository;
import com.taskmanager.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de administración de proyectos.
 */
@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public ProyectoService(
            ProyectoRepository proyectoRepository,
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository) {
        this.proyectoRepository = proyectoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ProyectoResponseDTO> listarEmpresaActual() {
        Long empresaId = TenantContext.getEmpresaId();
        return proyectoRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(ProyectoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    @Auditable(accion = "CREATE", entidad = "PROYECTO")
    public ProyectoResponseDTO crear(ProyectoCreateDTO dto) {
        validarPermisoGestion();
        Long empresaId = TenantContext.getEmpresaId();

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa", empresaId));

        Usuario lider = null;
        if (dto.liderId() != null) {
            lider = usuarioRepository.findByIdAndEmpresaId(dto.liderId(), empresaId)
                    .orElseThrow(() -> new TenantAccessException("Usuario", dto.liderId()));
        }

        Proyecto proyecto = Proyecto.builder()
                .empresa(empresa)
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .fechaFinEstimada(dto.fechaFinEstimada())
                .lider(lider)
                .build();

        return ProyectoResponseDTO.fromEntity(proyectoRepository.save(proyecto));
    }

    private void validarPermisoGestion() {
        RolUsuario rol = TenantContext.getRol();
        if (rol != RolUsuario.ADMIN && rol != RolUsuario.LIDER) {
            throw new ForbiddenRoleException("gestionar proyectos", "ADMIN o LIDER");
        }
    }
}
