package com.taskmanager.service;

import com.taskmanager.domain.entity.Comentario;
import com.taskmanager.domain.entity.Empresa;
import com.taskmanager.domain.entity.Proyecto;
import com.taskmanager.domain.entity.Tarea;
import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.domain.enums.EstadoTarea;
import com.taskmanager.domain.enums.Prioridad;
import com.taskmanager.domain.enums.RolUsuario;
import com.taskmanager.dto.request.AsignacionDTO;
import com.taskmanager.dto.request.CambioEstadoDTO;
import com.taskmanager.dto.request.DecisionRevisionDTO;
import com.taskmanager.dto.request.TareaCreateDTO;
import com.taskmanager.dto.response.TareaResponseDTO;
import com.taskmanager.exception.BusinessException;
import com.taskmanager.exception.EntityNotFoundException;
import com.taskmanager.exception.ForbiddenRoleException;
import com.taskmanager.exception.TenantAccessException;
import com.taskmanager.repository.ComentarioRepository;
import com.taskmanager.repository.EmpresaRepository;
import com.taskmanager.repository.ProyectoRepository;
import com.taskmanager.repository.TareaRepository;
import com.taskmanager.repository.UsuarioRepository;
import com.taskmanager.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TareaService tareaService;

    private Empresa empresa;
    private Usuario usuario;
    private Usuario lider;
    private Proyecto proyecto;
    private Tarea tarea;

    private MockedStatic<TenantContext> tenantContextMock;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getEmpresaId).thenReturn(1L);
        tenantContextMock.when(TenantContext::getUsuarioId).thenReturn(1L);
        tenantContextMock.when(TenantContext::getRol).thenReturn(RolUsuario.LIDER);
        tenantContextMock.when(TenantContext::isLiderOrAdmin).thenReturn(true);
        tenantContextMock.when(TenantContext::canReview).thenReturn(true);

        empresa = Empresa.builder()
                .id(1L)
                .nombre("Test Company")
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .nombreCompleto("Test User")
                .email("user@test.com")
                .empresa(empresa)
                .rol(RolUsuario.USER)
                .activo(true)
                .build();

        lider = Usuario.builder()
                .id(2L)
                .nombreCompleto("Lider User")
                .email("lider@test.com")
                .empresa(empresa)
                .rol(RolUsuario.LIDER)
                .activo(true)
                .build();

        proyecto = Proyecto.builder()
                .id(1L)
                .titulo("Test Project")
                .empresa(empresa)
                .lider(lider)
                .build();

        tarea = Tarea.builder()
                .id(1L)
                .titulo("Test Task")
                .descripcion("Test Description")
                .empresa(empresa)
                .proyecto(proyecto)
                .creador(lider)
                .estado(EstadoTarea.PENDIENTE)
                .prioridad(Prioridad.MEDIA)
                .requiereRevision(true)
                .eliminada(false)
                .build();
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test
    @DisplayName("crear - should create task successfully")
    void crear_ShouldCreateTask() {
        TareaCreateDTO dto = new TareaCreateDTO(
                1L,
                "New Task",
                "Description",
                120,
                LocalDateTime.now().plusDays(7),
                Prioridad.ALTA,
                true
        );

        when(proyectoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proyecto));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(invocation -> {
            Tarea t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        TareaResponseDTO result = tareaService.crear(dto);

        assertThat(result).isNotNull();
        assertThat(result.titulo()).isEqualTo("New Task");
        assertThat(result.estado()).isEqualTo(EstadoTarea.PENDIENTE);
        verify(tareaRepository).save(any(Tarea.class));
    }

    @Test
    @DisplayName("crear - should fail when project not found in tenant")
    void crear_ShouldFail_WhenProjectNotFound() {
        TareaCreateDTO dto = new TareaCreateDTO(
                999L,
                "New Task",
                "Description",
                null,
                LocalDateTime.now().plusDays(7),
                Prioridad.ALTA,
                true
        );

        when(proyectoRepository.findByIdAndEmpresaId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tareaService.crear(dto))
                .isInstanceOf(TenantAccessException.class)
                .hasMessageContaining("Proyecto");
    }

    @Test
    @DisplayName("asignar - should assign user and move to ASIGNADA")
    void asignar_ShouldAssignAndChangeState() {
        AsignacionDTO dto = new AsignacionDTO(1L, Prioridad.ALTA, LocalDateTime.now().plusDays(3));
        tarea.setEstado(EstadoTarea.PENDIENTE);

        when(tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(1L, 1L)).thenReturn(Optional.of(tarea));
        when(usuarioRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(usuario));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        TareaResponseDTO result = tareaService.asignar(1L, dto);

        assertThat(result.estado()).isEqualTo(EstadoTarea.ASIGNADA);
        verify(notificationService).notificarAsignacion(tarea, usuario);
    }

    @Test
    @DisplayName("asignar - should fail when task state is invalid")
    void asignar_ShouldFail_WhenWrongState() {
        AsignacionDTO dto = new AsignacionDTO(1L, Prioridad.MEDIA, null);
        tarea.setEstado(EstadoTarea.COMPLETADA);

        when(tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(1L, 1L)).thenReturn(Optional.of(tarea));
        when(usuarioRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> tareaService.asignar(1L, dto))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("asignar - should fail for cross-tenant user")
    void asignar_ShouldFail_WhenUserFromDifferentTenant() {
        AsignacionDTO dto = new AsignacionDTO(999L, Prioridad.MEDIA, null);

        when(tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(1L, 1L)).thenReturn(Optional.of(tarea));
        when(usuarioRepository.findByIdAndEmpresaId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tareaService.asignar(1L, dto))
                .isInstanceOf(TenantAccessException.class)
                .hasMessageContaining("Usuario");
    }

    @Test
    @DisplayName("cambiarEstado - should accept assigned task")
    void cambiarEstado_ShouldAcceptTask() {
        CambioEstadoDTO dto = new CambioEstadoDTO(CambioEstadoDTO.AccionEstado.ACEPTAR, null);
        tarea.setEstado(EstadoTarea.ASIGNADA);
        tarea.setUsuarioAsignado(usuario);

        tenantContextMock.when(TenantContext::getUsuarioId).thenReturn(1L);
        when(tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(1L, 1L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        TareaResponseDTO result = tareaService.cambiarEstado(1L, dto);

        assertThat(result.estado()).isEqualTo(EstadoTarea.EN_PROCESO);
        verify(tareaRepository).save(any(Tarea.class));
    }

    @Test
    @DisplayName("cambiarEstado - should reject assigned task and notify leader")
    void cambiarEstado_ShouldRejectTaskWithReason() {
        CambioEstadoDTO dto = new CambioEstadoDTO(CambioEstadoDTO.AccionEstado.RECHAZAR, "Not ready");
        tarea.setEstado(EstadoTarea.ASIGNADA);
        tarea.setUsuarioAsignado(usuario);

        tenantContextMock.when(TenantContext::getUsuarioId).thenReturn(1L);
        when(tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(1L, 1L)).thenReturn(Optional.of(tarea));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(mock(Comentario.class));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        TareaResponseDTO result = tareaService.cambiarEstado(1L, dto);

        assertThat(result.estado()).isEqualTo(EstadoTarea.BLOQUEADA);
        verify(comentarioRepository).save(any(Comentario.class));
        verify(notificationService).notificarBloqueo(tarea, lider);
    }

    @Test
    @DisplayName("cambiarEstado - should fail when user is not assigned")
    void cambiarEstado_ShouldFail_WhenUserNotAssigned() {
        CambioEstadoDTO dto = new CambioEstadoDTO(CambioEstadoDTO.AccionEstado.ACEPTAR, null);
        tarea.setEstado(EstadoTarea.ASIGNADA);
        tarea.setUsuarioAsignado(lider);

        tenantContextMock.when(TenantContext::getUsuarioId).thenReturn(1L);
        when(tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(1L, 1L)).thenReturn(Optional.of(tarea));

        assertThatThrownBy(() -> tareaService.cambiarEstado(1L, dto))
                .isInstanceOf(ForbiddenRoleException.class);
    }

    @Test
    @DisplayName("decidirRevision - should approve task")
    void decidirRevision_ShouldApproveTask() {
        DecisionRevisionDTO dto = new DecisionRevisionDTO(true, "Great work");
        tarea.setEstado(EstadoTarea.EN_REVISION);
        tarea.setUsuarioAsignado(usuario);

        when(tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(1L, 1L)).thenReturn(Optional.of(tarea));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lider));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(mock(Comentario.class));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        TareaResponseDTO result = tareaService.decidirRevision(1L, dto);

        assertThat(result.estado()).isEqualTo(EstadoTarea.APROBADA);
        verify(notificationService).notificarAprobacion(tarea);
    }

    @Test
    @DisplayName("decidirRevision - should reject task and notify assigned user")
    void decidirRevision_ShouldRejectTask() {
        DecisionRevisionDTO dto = new DecisionRevisionDTO(false, "Needs improvements");
        tarea.setEstado(EstadoTarea.EN_REVISION);
        tarea.setUsuarioAsignado(usuario);

        when(tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(1L, 1L)).thenReturn(Optional.of(tarea));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lider));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(mock(Comentario.class));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        TareaResponseDTO result = tareaService.decidirRevision(1L, dto);

        assertThat(result.estado()).isEqualTo(EstadoTarea.EN_PROCESO);
        verify(notificationService).notificarRechazoRevision(tarea, "Needs improvements");
    }

    @Test
    @DisplayName("decidirRevision - should fail when rejecting without comment")
    void decidirRevision_ShouldFail_WhenRejectingWithoutComment() {
        DecisionRevisionDTO dto = new DecisionRevisionDTO(false, null);
        tarea.setEstado(EstadoTarea.EN_REVISION);

        when(tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(1L, 1L)).thenReturn(Optional.of(tarea));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lider));

        assertThatThrownBy(() -> tareaService.decidirRevision(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("comentario");
    }

    @Test
    @DisplayName("decidirRevision - should fail when role cannot review")
    void decidirRevision_ShouldFail_WhenUserRoleTriesToReview() {
        tenantContextMock.when(TenantContext::canReview).thenReturn(false);

        assertThatThrownBy(() -> tareaService.decidirRevision(1L, new DecisionRevisionDTO(true, "Approved")))
                .isInstanceOf(ForbiddenRoleException.class);
    }

    @Test
    @DisplayName("obtenerPorId - should fail when task belongs to different tenant")
    void obtenerPorId_ShouldFail_WhenDifferentTenant() {
        when(tareaRepository.findByIdAndEmpresaIdAndEliminadaFalse(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tareaService.obtenerPorId(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("listarTodas - should only return active tasks from current tenant")
    void listarTodas_ShouldReturnOnlyTenantTasks() {
        when(tareaRepository.findAllByEmpresaIdAndEliminadaFalse(1L)).thenReturn(List.of(tarea));

        List<TareaResponseDTO> result = tareaService.listarTodas();

        assertThat(result).hasSize(1);
        verify(tareaRepository).findAllByEmpresaIdAndEliminadaFalse(1L);
    }

    @Test
    @DisplayName("state machine - PENDIENTE to ASIGNADA")
    void stateMachine_PendienteToAsignada() {
        tarea.setEstado(EstadoTarea.PENDIENTE);

        assertThatCode(() -> tarea.asignar(usuario, Prioridad.MEDIA, LocalDateTime.now().plusDays(1)))
                .doesNotThrowAnyException();
        assertThat(tarea.getEstado()).isEqualTo(EstadoTarea.ASIGNADA);
    }

    @Test
    @DisplayName("state machine - ASIGNADA to EN_PROCESO")
    void stateMachine_AsignadaToEnProceso() {
        tarea.setEstado(EstadoTarea.ASIGNADA);

        tarea.aceptar();

        assertThat(tarea.getEstado()).isEqualTo(EstadoTarea.EN_PROCESO);
    }

    @Test
    @DisplayName("state machine - ASIGNADA to BLOQUEADA")
    void stateMachine_AsignadaToBloqueada() {
        tarea.setEstado(EstadoTarea.ASIGNADA);

        tarea.rechazar();

        assertThat(tarea.getEstado()).isEqualTo(EstadoTarea.BLOQUEADA);
    }

    @Test
    @DisplayName("state machine - EN_PROCESO to EN_REVISION when review required")
    void stateMachine_EnProcesoToEnRevision() {
        tarea.setEstado(EstadoTarea.EN_PROCESO);
        tarea.setRequiereRevision(true);

        tarea.entregarTrabajo();

        assertThat(tarea.getEstado()).isEqualTo(EstadoTarea.EN_REVISION);
    }

    @Test
    @DisplayName("state machine - EN_PROCESO to COMPLETADA when no review required")
    void stateMachine_EnProcesoToCompletada() {
        tarea.setEstado(EstadoTarea.EN_PROCESO);
        tarea.setRequiereRevision(false);

        tarea.entregarTrabajo();

        assertThat(tarea.getEstado()).isEqualTo(EstadoTarea.COMPLETADA);
    }

    @Test
    @DisplayName("state machine - EN_REVISION approved goes to APROBADA")
    void stateMachine_EnRevisionApprovedToAprobada() {
        tarea.setEstado(EstadoTarea.EN_REVISION);

        tarea.aprobar();

        assertThat(tarea.getEstado()).isEqualTo(EstadoTarea.APROBADA);
    }

    @Test
    @DisplayName("state machine - EN_REVISION rejected goes to EN_PROCESO")
    void stateMachine_EnRevisionRejectedToEnProceso() {
        tarea.setEstado(EstadoTarea.EN_REVISION);

        tarea.rechazarRevision();

        assertThat(tarea.getEstado()).isEqualTo(EstadoTarea.EN_PROCESO);
    }
}
