package com.example.backend_v2.service;

import com.example.backend_v2.model.entity.Empresa;
import com.example.backend_v2.model.entity.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private AuthService authService;

    @Mock
    private Authentication mockAuth;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUsuarioActual_conUsuarioPrincipal_devuelveUsuario() {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@coflow.com");

        SecurityContext ctx = new SecurityContextImpl(mockAuth);
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getPrincipal()).thenReturn(usuario);
        SecurityContextHolder.setContext(ctx);

        Usuario resultado = authService.getUsuarioActual();

        assertNotNull(resultado);
        assertEquals("user@coflow.com", resultado.getEmail());
    }

    @Test
    void getUsuarioActual_conPrincipalNoUsuario_devuelveNull() {
        SecurityContext ctx = new SecurityContextImpl(mockAuth);
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getPrincipal()).thenReturn("anonymous");
        SecurityContextHolder.setContext(ctx);

        assertNull(authService.getUsuarioActual());
    }

    @Test
    void getUsuarioActual_sinAutenticacion_devuelveNull() {
        SecurityContextHolder.clearContext();

        assertNull(authService.getUsuarioActual());
    }

    @Test
    void getEmpresaIdActual_conUsuarioConEmpresa_devuelveId() {
        Empresa empresa = new Empresa();
        empresa.setId(42L);

        Usuario usuario = new Usuario();
        usuario.setEmpresa(empresa);

        SecurityContext ctx = new SecurityContextImpl(mockAuth);
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getPrincipal()).thenReturn(usuario);
        SecurityContextHolder.setContext(ctx);

        Long empresaId = authService.getEmpresaIdActual();

        assertEquals(42L, empresaId);
    }

    @Test
    void getEmpresaIdActual_sinUsuario_devuelveNull() {
        SecurityContextHolder.clearContext();

        assertNull(authService.getEmpresaIdActual());
    }
}
