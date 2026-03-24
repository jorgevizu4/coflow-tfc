package com.taskmanager.service;

import com.taskmanager.aspect.Auditable;
import com.taskmanager.domain.entity.*;
import com.taskmanager.domain.enums.EstadoTarea;
import com.taskmanager.dto.response.EntregableResponseDTO;
import com.taskmanager.exception.BusinessException;
import com.taskmanager.exception.EntityNotFoundException;
import com.taskmanager.exception.InvalidStateTransitionException;
import com.taskmanager.exception.TenantAccessException;
import com.taskmanager.repository.*;
import com.taskmanager.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Servicio para gestión de entregables.
 */
@Service
public class EntregableService {

    private static final Logger log = LoggerFactory.getLogger(EntregableService.class);

    private final EntregableRepository entregableRepository;
    private final TareaRepository tareaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public EntregableService(
            EntregableRepository entregableRepository,
            TareaRepository tareaRepository,
            UsuarioRepository usuarioRepository,
            FileStorageService fileStorageService,
            NotificationService notificationService) {
        this.entregableRepository = entregableRepository;
        this.tareaRepository = tareaRepository;
        this.usuarioRepository = usuarioRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    /**
     * Sube un entregable para una tarea.
     * Implementa la transición automática según requiereRevision.
     */
    @Transactional
    @Auditable(accion = "UPLOAD_ENTREGABLE", entidad = "ENTREGABLE")
    public EntregableResponseDTO subirEntregable(Long tareaId, MultipartFile archivo, String comentarios) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        // Obtener tarea validando tenant
        Tarea tarea = tareaRepository.findByIdAndEmpresaId(tareaId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Tarea", tareaId));

        // Validar estado de la tarea
        if (tarea.getEstado() != EstadoTarea.EN_PROCESO) {
            throw new InvalidStateTransitionException(tarea.getEstado().name(), "entregar documentos");
        }

        // Validar que el usuario es el asignado
        if (tarea.getUsuarioAsignado() == null || !tarea.getUsuarioAsignado().getId().equals(usuarioId)) {
            throw new BusinessException("UNAUTHORIZED_DELIVERY", "Solo el usuario asignado puede entregar documentos");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", usuarioId));

        try {
            // Almacenar archivo
            String urlArchivo = fileStorageService.almacenarArchivo(archivo, empresaId, tareaId);

            // Calcular versión
            int nuevaVersion = (int) entregableRepository.countByTareaId(tareaId) + 1;

            // Crear entregable
            Entregable entregable = Entregable.builder()
                    .empresa(tarea.getEmpresa())
                    .tarea(tarea)
                    .usuario(usuario)
                    .urlArchivo(urlArchivo)
                    .nombreArchivo(archivo.getOriginalFilename())
                    .tipoContenido(archivo.getContentType())
                    .tamanoBytes(archivo.getSize())
                    .comentarios(comentarios)
                    .version(nuevaVersion)
                    .build();

            entregable = entregableRepository.save(entregable);

            // Transición automática de estado
            tarea.entregarTrabajo();
            tareaRepository.save(tarea);

            // Notificar según el nuevo estado
            if (tarea.getEstado() == EstadoTarea.EN_REVISION && tarea.getProyecto().getLider() != null) {
                notificationService.notificarListaParaRevision(tarea, tarea.getProyecto().getLider());
            }

            log.info("Entregable {} subido para tarea {}. Nuevo estado: {}", 
                    entregable.getId(), tareaId, tarea.getEstado());

            return EntregableResponseDTO.fromEntity(entregable);

        } catch (IOException e) {
            log.error("Error al almacenar archivo para tarea {}", tareaId, e);
            throw new BusinessException("FILE_STORAGE_ERROR", "Error al almacenar el archivo: " + e.getMessage());
        }
    }

    /**
     * Lista entregables de una tarea.
     */
    @Transactional(readOnly = true)
    public List<EntregableResponseDTO> listarPorTarea(Long tareaId) {
        Long empresaId = TenantContext.getEmpresaId();

        // Validar acceso a la tarea
        tareaRepository.findByIdAndEmpresaId(tareaId, empresaId)
                .orElseThrow(() -> new TenantAccessException("Tarea", tareaId));

        List<Entregable> entregables = entregableRepository.findAllByTareaId(tareaId);
        return EntregableResponseDTO.fromEntities(entregables);
    }

    /**
     * Obtiene un entregable por ID.
     */
    @Transactional(readOnly = true)
    public EntregableResponseDTO obtenerPorId(Long id) {
        Long empresaId = TenantContext.getEmpresaId();

        Entregable entregable = entregableRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Entregable", id));

        return EntregableResponseDTO.fromEntity(entregable);
    }
}
