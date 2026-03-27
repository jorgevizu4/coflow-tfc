package com.example.backend_v2.service;

import com.example.backend_v2.model.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@Service
public class AuthService {

    public Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        return null;
    }

    public Long getEmpresaIdActual() {
        Usuario usuario = getUsuarioActual();
        return (usuario != null && usuario.getEmpresa() != null) ? usuario.getEmpresa().getId() : null;
    }
}
