package com.taskmanager.security;

import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.domain.enums.RolUsuario;
import com.taskmanager.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de autenticación JWT.
 * Valida el token y establece el contexto de seguridad y tenant.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            // Si no hay header de autorización o no es Bearer, continuar sin autenticar
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(BEARER_PREFIX.length());

            // Validar token
            if (!jwtService.isTokenValid(jwt)) {
                log.warn("Token JWT inválido o expirado");
                filterChain.doFilter(request, response);
                return;
            }

            // Extraer información del token
            Long usuarioId = jwtService.extractUsuarioId(jwt);
            Long empresaId = jwtService.extractEmpresaId(jwt);
            String rolString = jwtService.extractRol(jwt);

            // Solo autenticar si no hay autenticación previa
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Verificar que el usuario existe y está activo
                Usuario usuario = usuarioRepository.findById(usuarioId)
                        .filter(Usuario::getActivo)
                        .orElse(null);

                if (usuario == null) {
                    log.warn("Usuario {} no encontrado o inactivo", usuarioId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Verificar que el usuario pertenece a la empresa del token
                if (!usuario.getEmpresa().getId().equals(empresaId)) {
                    log.warn("Usuario {} no pertenece a empresa {}", usuarioId, empresaId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Establecer contexto de tenant
                RolUsuario rol = RolUsuario.valueOf(rolString);
                TenantContext.setTenant(empresaId, usuarioId, rol);

                // Crear autenticación de Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol.name()))
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Usuario {} autenticado con rol {} en empresa {}", usuarioId, rol, empresaId);
            }

            filterChain.doFilter(request, response);
            
        } finally {
            // Limpiar contexto de tenant al finalizar la petición
            TenantContext.clear();
        }
    }
}
