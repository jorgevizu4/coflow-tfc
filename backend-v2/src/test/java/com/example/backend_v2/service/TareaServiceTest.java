package com.example.backend_v2.service;

import com.example.backend_v2.dto.*;
import com.example.backend_v2.model.entity.Empresa;
import com.example.backend_v2.model.entity.Proyecto;
import com.example.backend_v2.model.entity.Tarea;
import com.example.backend_v2.model.entity.Usuario;
import com.example.backend_v2.model.enums.EstadoTarea;
import com.example.backend_v2.model.enums.Prioridad;
import com.example.backend_v2.repository.ProyectoRepository;
import com.example.backend_v2.repository.TareaRepository;
import com.example.backend_v2.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock private TareaRepository tareaRepository;
    @Mock private ProyectoRepository proyectoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuthService authService;

    @InjectMocks
    private TareaService tareaService;

    private Empresa empresa;
    private Usuario usuario;
    private Proyecto proyecto;
    private Tarea tarea;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Empresa Test");

        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNombre("Ana");
        usuario.setEmail("ana@coflow.com");
        usuario.setEmpresa(empresa);

        proyecto = new Proyecto();
        proyecto.setId(100L);
        proyecto.setNombre("Proyecto Test");
        proyecto.setTitulo("Proyecto Test");
        proyecto.setEmpresa(empresa);

        tarea = new Tarea();
        tarea.setId(1000L);
        tarea.setTitulo("Implementar login");
        tarea.setNombre("Implementar login");
        tarea.setDescripcion("Descripción de prueba");
        tarea.setEstadoTarea(EstadoTarea.PENDIENTE);
        tarea.setPrioridad(Prioridad.MEDIA);
        tarea.setProyecto(proyecto);
        tarea.setCreadoPor(usuario);

        lenient().when(authService.getEmpresaIdActual()).thenReturn(1L);
        lenient().when(authService.getUsuarioActual()).thenReturn(usuario);
    }

    // ──────────────────────────────── findAll ────────────────────────────────

    @Test
    void findAll_devuelveTareasDeEmpresa() {
        when(tareaRepository.findByEmpresaId(1L)).thenReturn(List.of(tarea));

        List<TareaDTO> result = tareaService.findAll();

        assertEquals(1, result.size());
        assertEquals("Implementar login", result.get(0).getTitulo());
    }

    @Test
    void findAll_sinTareas_devuelveListaVacia() {
        when(tareaRepository.findByEmpresaId(1L)).thenReturn(List.of());

        assertTrue(tareaService.findAll().isEmpty());
    }

    // ──────────────────────────────── findByProyectoId ───────────────────────

    @Test
    void findByProyectoId_devuelveTareasDelProyecto() {
        when(tareaRepository.findByProyectoId(100L)).thenReturn(List.of(tarea));

        List<TareaDTO> result = tareaService.findByProyectoId(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getProyectoId());
    }

    // ──────────────────────────────── findById ───────────────────────────────

    @Test
    void findById_cuandoPerteneceAEmpresa_devuelveDTO() {
        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));

        Optional<TareaDTO> result = tareaService.findById(1000L);

        assertTrue(result.isPresent());
        assertEquals(1000L, result.get().getId());
    }

    @Test
    void findById_cuandoPerteneceADistintaEmpresa_devuelveVacio() {
        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(99L);
        proyecto.setEmpresa(otraEmpresa);

        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));

        Optional<TareaDTO> result = tareaService.findById(1000L);

        assertFalse(result.isPresent());
    }

    // ──────────────────────────────── misTareas ──────────────────────────────

    @Test
    void misTareas_devuelveTareasAsignadasAlUsuarioActual() {
        when(tareaRepository.findMisTareas(1L, 10L)).thenReturn(List.of(tarea));

        List<TareaDTO> result = tareaService.misTareas();

        assertEquals(1, result.size());
    }

    // ──────────────────────────────── pendientesRevision ─────────────────────

    @Test
    void pendientesRevision_devuelveTareasEnRevision() {
        tarea.setEstadoTarea(EstadoTarea.EN_REVISION);
        when(tareaRepository.findByEmpresaIdAndEstado(1L, EstadoTarea.EN_REVISION))
                .thenReturn(List.of(tarea));

        List<TareaDTO> result = tareaService.pendientesRevision();

        assertEquals(1, result.size());
        assertEquals(EstadoTarea.EN_REVISION, result.get(0).getEstado());
    }

    // ──────────────────────────────── getSeguimiento ─────────────────────────

    @Test
    void getSeguimiento_devuelveConteoPorEstado() {
        Tarea t2 = new Tarea();
        t2.setId(1001L);
        t2.setEstadoTarea(EstadoTarea.EN_PROCESO);
        t2.setProyecto(proyecto);

        when(tareaRepository.findByEmpresaId(1L)).thenReturn(List.of(tarea, t2));

        Map<String, Long> seguimiento = tareaService.getSeguimiento();

        assertEquals(1L, seguimiento.get("PENDIENTE"));
        assertEquals(1L, seguimiento.get("EN_PROCESO"));
        assertEquals(0L, seguimiento.get("COMPLETADA"));
    }

    // ──────────────────────────────── crear ──────────────────────────────────

    @Test
    void crear_conProyectoValido_creaYDevuelveTarea() {
        TareaCrearRequest req = new TareaCrearRequest(100L, "Nueva tarea", "Desc",
                5, "2026-06-01", Prioridad.ALTA, false, null);

        when(proyectoRepository.findById(100L)).thenReturn(Optional.of(proyecto));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> {
            Tarea t = inv.getArgument(0);
            t.setId(2000L);
            return t;
        });

        TareaDTO result = tareaService.crear(req);

        assertEquals("Nueva tarea", result.getTitulo());
        assertEquals(EstadoTarea.PENDIENTE, result.getEstado());
        verify(tareaRepository, times(1)).save(any(Tarea.class));
    }

    @Test
    void crear_conProyectoDeOtraEmpresa_lanza404() {
        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(99L);
        proyecto.setEmpresa(otraEmpresa);

        TareaCrearRequest req = new TareaCrearRequest(100L, "T", "D",
                1, null, Prioridad.BAJA, false, null);

        when(proyectoRepository.findById(100L)).thenReturn(Optional.of(proyecto));

        assertThrows(ResponseStatusException.class, () -> tareaService.crear(req));
    }

    @Test
    void crear_conProyectoNoExistente_lanza404() {
        TareaCrearRequest req = new TareaCrearRequest(999L, "T", "D",
                1, null, Prioridad.BAJA, false, null);

        when(proyectoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> tareaService.crear(req));
    }

    // ──────────────────────────────── asignar ────────────────────────────────

    @Test
    void asignar_estableceUsuarioYCambiaEstadoAAsignada() {
        Usuario asignado = new Usuario();
        asignado.setId(20L);
        asignado.setNombre("Carlos");

        AsignacionRequest req = new AsignacionRequest(20L, Prioridad.URGENTE, "2026-07-01");

        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(asignado));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        TareaDTO result = tareaService.asignar(1000L, req);

        assertEquals(EstadoTarea.ASIGNADA, result.getEstado());
        assertEquals("Carlos", result.getUsuarioAsignado().getNombreCompleto());
    }

    @Test
    void asignar_conUsuarioNoExistente_lanza404() {
        AsignacionRequest req = new AsignacionRequest(999L, null, null);

        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> tareaService.asignar(1000L, req));
    }

    // ──────────────────────────────── cambiarEstado ──────────────────────────

    @Test
    void cambiarEstado_aceptar_estableceEnProceso() {
        tarea.setEstadoTarea(EstadoTarea.ASIGNADA);
        CambioEstadoRequest req = new CambioEstadoRequest("ACEPTAR", null);

        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        TareaDTO result = tareaService.cambiarEstado(1000L, req);

        assertEquals(EstadoTarea.EN_PROCESO, result.getEstado());
    }

    @Test
    void cambiarEstado_rechazar_establecePendienteYLimpiaAsignado() {
        tarea.setEstadoTarea(EstadoTarea.ASIGNADA);
        tarea.setUsuarioAsignado(usuario);
        CambioEstadoRequest req = new CambioEstadoRequest("RECHAZAR", "No aplica");

        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        TareaDTO result = tareaService.cambiarEstado(1000L, req);

        assertEquals(EstadoTarea.PENDIENTE, result.getEstado());
        assertNull(result.getUsuarioAsignado());
    }

    @Test
    void cambiarEstado_accionInvalida_lanza400() {
        CambioEstadoRequest req = new CambioEstadoRequest("IGNORAR", null);

        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));

        assertThrows(ResponseStatusException.class,
                () -> tareaService.cambiarEstado(1000L, req));
    }

    // ──────────────────────────────── moverEstado ────────────────────────────

    @Test
    void moverEstado_actualizaEstado() {
        MoverEstadoRequest req = new MoverEstadoRequest(EstadoTarea.BLOQUEADA, "Bloqueada por dependencia");

        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        TareaDTO result = tareaService.moverEstado(1000L, req);

        assertEquals(EstadoTarea.BLOQUEADA, result.getEstado());
    }

    // ──────────────────────────────── decidirRevision ────────────────────────

    @Test
    void decidirRevision_aprobado_estableceAprobada() {
        tarea.setEstadoTarea(EstadoTarea.EN_REVISION);
        DecisionRevisionRequest req = new DecisionRevisionRequest(true, "Todo correcto");

        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        TareaDTO result = tareaService.decidirRevision(1000L, req);

        assertEquals(EstadoTarea.APROBADA, result.getEstado());
    }

    @Test
    void decidirRevision_rechazado_estableceRechazada() {
        tarea.setEstadoTarea(EstadoTarea.EN_REVISION);
        DecisionRevisionRequest req = new DecisionRevisionRequest(false, "Faltan pruebas");

        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        TareaDTO result = tareaService.decidirRevision(1000L, req);

        assertEquals(EstadoTarea.RECHAZADA, result.getEstado());
    }

    // ──────────────────────────────── deleteById ─────────────────────────────

    @Test
    void deleteById_cuandoExiste_eliminaCorrectamente() {
        when(tareaRepository.findById(1000L)).thenReturn(Optional.of(tarea));

        assertDoesNotThrow(() -> tareaService.deleteById(1000L));
        verify(tareaRepository, times(1)).deleteById(1000L);
    }

    @Test
    void deleteById_cuandoNoExiste_lanza404() {
        when(tareaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> tareaService.deleteById(999L));
        verify(tareaRepository, never()).deleteById(any());
    }
}
