package com.taskmanager.security;

import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.domain.enums.RolUsuario;

/**
 * Contexto del tenant actual (ThreadLocal).
 * Almacena información del usuario autenticado para el request actual.
 */
public class TenantContext {

    private static final ThreadLocal<TenantInfo> CONTEXT = new ThreadLocal<>();

    public static void setTenant(Long empresaId, Long usuarioId, RolUsuario rol) {
        CONTEXT.set(new TenantInfo(empresaId, usuarioId, rol));
    }

    public static void setTenant(Usuario usuario) {
        CONTEXT.set(new TenantInfo(
                usuario.getEmpresa().getId(),
                usuario.getId(),
                usuario.getRol()
        ));
    }

    public static TenantInfo getTenant() {
        return CONTEXT.get();
    }

    public static Long getEmpresaId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.empresaId() : null;
    }

    public static Long getUsuarioId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.usuarioId() : null;
    }

    public static RolUsuario getRol() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.rol() : null;
    }

    public static boolean isAdmin() {
        return getRol() == RolUsuario.ADMIN;
    }

    public static boolean isLiderOrAdmin() {
        RolUsuario rol = getRol();
        return rol == RolUsuario.ADMIN || rol == RolUsuario.LIDER;
    }

    public static boolean canReview() {
        RolUsuario rol = getRol();
        return rol == RolUsuario.ADMIN || rol == RolUsuario.LIDER || rol == RolUsuario.REVISOR;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * Record inmutable con información del tenant.
     */
    public record TenantInfo(Long empresaId, Long usuarioId, RolUsuario rol) {}
}
