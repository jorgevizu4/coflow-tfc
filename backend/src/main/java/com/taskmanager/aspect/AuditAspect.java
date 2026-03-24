package com.taskmanager.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.domain.entity.Empresa;
import com.taskmanager.domain.entity.RegistroActividad;
import com.taskmanager.domain.entity.Usuario;
import com.taskmanager.repository.EmpresaRepository;
import com.taskmanager.repository.RegistroActividadRepository;
import com.taskmanager.repository.UsuarioRepository;
import com.taskmanager.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspecto AOP para auditoría automática.
 * Intercepta métodos anotados con @Auditable y registra la actividad.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final RegistroActividadRepository registroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(
            RegistroActividadRepository registroRepository,
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository) {
        this.registroRepository = registroRepository;
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Around("@annotation(auditable)")
    public Object auditarMetodo(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // Capturar información antes de ejecutar
        String metodo = obtenerFirmaMetodo(joinPoint);
        String argumentosJson = serializarArgumentos(joinPoint.getArgs());
        
        Object resultado = null;
        Exception excepcion = null;
        
        try {
            // Ejecutar el método original
            resultado = joinPoint.proceed();
            return resultado;
            
        } catch (Exception e) {
            excepcion = e;
            throw e;
            
        } finally {
            // Registrar auditoría después de ejecutar
            try {
                registrarAuditoria(auditable, metodo, argumentosJson, resultado, excepcion);
            } catch (Exception e) {
                log.error("Error al registrar auditoría: {}", e.getMessage());
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Auditoría completada para {} en {}ms", metodo, duration);
        }
    }

    private void registrarAuditoria(
            Auditable auditable,
            String metodo,
            String argumentosJson,
            Object resultado,
            Exception excepcion) {
        
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();
        
        // Si no hay contexto de tenant, no auditar
        if (empresaId == null || usuarioId == null) {
            log.debug("Sin contexto de tenant, omitiendo auditoría para {}", metodo);
            return;
        }

        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        // Extraer ID de entidad del resultado si es posible
        Long entidadId = extraerIdDeResultado(resultado);

        // Serializar resultado
        String resultadoJson = serializarResultado(resultado);

        // Obtener información del request
        String ipAddress = obtenerIpCliente();
        String userAgent = obtenerUserAgent();

        // Crear registro de auditoría
        RegistroActividad registro = RegistroActividad.builder()
                .empresa(empresa)
                .usuario(usuario)
                .entidad(auditable.entidad().isEmpty() ? inferirEntidad(metodo) : auditable.entidad())
                .entidadId(entidadId)
                .accion(auditable.accion())
                .metodo(metodo)
                .valoresAnteriores(argumentosJson)
                .valoresNuevos(resultadoJson)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .categoria(excepcion != null ? "ERROR" : "SUCCESS")
                .build();

        registroRepository.save(registro);
        
        log.info("Auditoría registrada: {} - {} - entidad={}, id={}", 
                auditable.accion(), metodo, registro.getEntidad(), entidadId);
    }

    private String obtenerFirmaMetodo(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringTypeName() + "." + signature.getName();
    }

    private String serializarArgumentos(Object[] args) {
        try {
            // Filtrar argumentos sensibles (passwords, etc.)
            Object[] argsFiltrados = filtrarArgumentosSensibles(args);
            return objectMapper.writeValueAsString(argsFiltrados);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String serializarResultado(Object resultado) {
        if (resultado == null) return null;
        try {
            return objectMapper.writeValueAsString(resultado);
        } catch (JsonProcessingException e) {
            return resultado.toString();
        }
    }

    private Object[] filtrarArgumentosSensibles(Object[] args) {
        Object[] filtrados = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                filtrados[i] = null;
            } else if (esArgumentoSensible(arg)) {
                filtrados[i] = "[REDACTED]";
            } else {
                filtrados[i] = arg;
            }
        }
        return filtrados;
    }

    private boolean esArgumentoSensible(Object arg) {
        String className = arg.getClass().getSimpleName().toLowerCase();
        return className.contains("password") || 
               className.contains("credential") ||
               className.contains("secret") ||
               className.contains("login");
    }

    private Long extraerIdDeResultado(Object resultado) {
        if (resultado == null) return null;
        
        try {
            // Intentar obtener el ID via reflexión
            var metodoGetId = resultado.getClass().getMethod("id");
            Object id = metodoGetId.invoke(resultado);
            if (id instanceof Long) {
                return (Long) id;
            }
        } catch (Exception ignored) {
            // Si no tiene método id(), intentar con getId()
            try {
                var metodoGetId = resultado.getClass().getMethod("getId");
                Object id = metodoGetId.invoke(resultado);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (Exception ignored2) {}
        }
        
        return null;
    }

    private String inferirEntidad(String metodo) {
        if (metodo.contains("Tarea")) return "TAREA";
        if (metodo.contains("Proyecto")) return "PROYECTO";
        if (metodo.contains("Usuario")) return "USUARIO";
        if (metodo.contains("Entregable")) return "ENTREGABLE";
        if (metodo.contains("Comentario")) return "COMENTARIO";
        return "SISTEMA";
    }

    private String obtenerIpCliente() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String forwardedFor = request.getHeader("X-Forwarded-For");
                if (forwardedFor != null && !forwardedFor.isEmpty()) {
                    return forwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String obtenerUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader("User-Agent");
            }
        } catch (Exception ignored) {}
        return null;
    }
}
