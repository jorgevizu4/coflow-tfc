package com.taskmanager.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar métodos que deben ser auditados.
 * El aspecto AuditAspect interceptará estos métodos y registrará la actividad.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Tipo de acción a registrar.
     */
    String accion() default "OPERATION";

    /**
     * Nombre de la entidad afectada.
     */
    String entidad() default "";

    /**
     * Descripción adicional de la operación.
     */
    String descripcion() default "";
}
