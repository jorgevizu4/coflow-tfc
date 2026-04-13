package com.example.backend_v2.service;

import com.example.backend_v2.dto.ProyectoCrearRequest;
import com.example.backend_v2.dto.ProyectoDTO;
import com.example.backend_v2.model.entity.Empresa;
import com.example.backend_v2.model.entity.Proyecto;
import com.example.backend_v2.model.entity.Usuario;
import com.example.backend_v2.repository.EmpresaRepository;
import com.example.backend_v2.repository.ProyectoRepository;
import com.example.backend_v2.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock private ProyectoRepository proyectoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private AuthService authService;

    @InjectMocks
    private ProyectoService proyectoService;

    private Empresa empresa;
    private Usuario lider;
    private Proyecto proyecto;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("CoFlow Demo");

        lider = new Usuario();
        lider.setId(5L);
        lider.setNombre("Laura");
        lider.setApellidos("Gómez");
        lider.setEmail("laura@coflow.com");
        lider.setEmpresa(empresa);

        proyecto = new Proyecto();
        proyecto.setId(100L);
        proyecto.setNombre("Sprint Alpha");
        proyecto.setTitulo("Sprint Alpha");
        proyecto.setDescripcion("Primer sprint del proyecto");
        proyecto.setEmpresa(empresa);
        proyecto.setLider(lider);

        lenient().when(authService.getEmpresaIdActual()).thenReturn(1L);
    }

    // ──────────────────────────────── findAll ────────────────────────────────

    @Test
    void findAll_devuelveProyectosDeEmpresa() {
        when(proyectoRepository.findByEmpresaId(1L)).thenReturn(List.of(proyecto));

        List<ProyectoDTO> result = proyectoService.findAll();

        assertEquals(1, result.size());
        assertEquals("Sprint Alpha", result.get(0).getTitulo());
        assertEquals("CoFlow Demo", result.get(0).getEmpresaNombre());
    }

    @Test
    void findAll_sinProyectos_devuelveListaVacia() {
        when(proyectoRepository.findByEmpresaId(1L)).thenReturn(List.of());

        assertTrue(proyectoService.findAll().isEmpty());
    }

    // ──────────────────────────────── findById ───────────────────────────────

    @Test
    void findById_cuandoPerteneceAEmpresa_devuelveDTO() {
        when(proyectoRepository.findById(100L)).thenReturn(Optional.of(proyecto));

        Optional<ProyectoDTO> result = proyectoService.findById(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getId());
        assertEquals("Laura Gómez", result.get().getLiderNombre());
    }

    @Test
    void findById_cuandoPerteneceADistintaEmpresa_devuelveVacio() {
        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(99L);
        proyecto.setEmpresa(otraEmpresa);

        when(proyectoRepository.findById(100L)).thenReturn(Optional.of(proyecto));

        Optional<ProyectoDTO> result = proyectoService.findById(100L);

        assertFalse(result.isPresent());
    }

    @Test
    void findById_noExistente_devuelveVacio() {
        when(proyectoRepository.findById(999L)).thenReturn(Optional.empty());

        assertFalse(proyectoService.findById(999L).isPresent());
    }

    // ──────────────────────────────── crear ──────────────────────────────────

    @Test
    void crear_sinLider_guardaProyectoSinLider() {
        ProyectoCrearRequest req = new ProyectoCrearRequest(
                "Nuevo Proyecto", "Descripción", "2026-12-31", null);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(inv -> {
            Proyecto p = inv.getArgument(0);
            p.setId(200L);
            return p;
        });

        ProyectoDTO result = proyectoService.crear(req);

        assertEquals("Nuevo Proyecto", result.getTitulo());
        assertNull(result.getLiderId());
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void crear_conLider_asignaLiderAlProyecto() {
        ProyectoCrearRequest req = new ProyectoCrearRequest(
                "Proyecto con Líder", "Desc", "2026-12-31", 5L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(lider));
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(inv -> {
            Proyecto p = inv.getArgument(0);
            p.setId(201L);
            return p;
        });

        ProyectoDTO result = proyectoService.crear(req);

        assertEquals(5L, result.getLiderId());
        assertEquals("Laura Gómez", result.getLiderNombre());
    }

    @Test
    void crear_conLiderNoExistente_lanza404() {
        ProyectoCrearRequest req = new ProyectoCrearRequest(
                "P", "D", null, 999L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> proyectoService.crear(req));
    }

    @Test
    void crear_cuandoEmpresaNoExiste_lanza404() {
        ProyectoCrearRequest req = new ProyectoCrearRequest(
                "P", "D", null, null);

        when(empresaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> proyectoService.crear(req));
        verify(proyectoRepository, never()).save(any());
    }

    // ──────────────────────────────── deleteById ─────────────────────────────

    @Test
    void deleteById_cuandoExiste_eliminaCorrectamente() {
        when(proyectoRepository.findById(100L)).thenReturn(Optional.of(proyecto));

        assertDoesNotThrow(() -> proyectoService.deleteById(100L));
        verify(proyectoRepository, times(1)).deleteById(100L);
    }

    @Test
    void deleteById_cuandoNoExiste_lanza404() {
        when(proyectoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> proyectoService.deleteById(999L));
        verify(proyectoRepository, never()).deleteById(any());
    }
}
