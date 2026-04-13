import { EstadoTarea, Prioridad } from "../../types/types";

export const ESTADO_LABEL: Record<EstadoTarea, string> = {
    PENDIENTE:   "Pendiente",
    ASIGNADA:    "Asignada",
    EN_PROCESO:  "En proceso",
    BLOQUEADA:   "Bloqueada",
    EN_REVISION: "En revisión",
    APROBADA:    "Aprobada",
    RECHAZADA:   "Rechazada",
    COMPLETADA:  "Completada",
};

export const ESTADO_BADGE: Record<EstadoTarea, string> = {
    PENDIENTE:   "secondary",
    ASIGNADA:    "primary",
    EN_PROCESO:  "info text-dark",
    BLOQUEADA:   "danger",
    EN_REVISION: "warning text-dark",
    APROBADA:    "success",
    RECHAZADA:   "danger",
    COMPLETADA:  "success",
};

export const PRIORIDAD_BADGE: Record<Prioridad, string> = {
    BAJA:    "success",
    MEDIA:   "warning text-dark",
    ALTA:    "danger",
    URGENTE: "dark",
};
