// ============ API envelope ============
export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

// ============ Enums ============
export type EstadoTarea =
    | 'PENDIENTE'
    | 'ASIGNADA'
    | 'EN_PROCESO'
    | 'BLOQUEADA'
    | 'EN_REVISION'
    | 'APROBADA'
    | 'RECHAZADA'
    | 'COMPLETADA';

export type Prioridad = 'BAJA' | 'MEDIA' | 'ALTA' | 'URGENTE';

export type RolUsuario = 'ADMIN' | 'LIDER' | 'REVISOR' | 'USER';

// ============ Auth ============
export interface LoginResponse {
    token: string;
    tipo: string;
    usuarioId: number;
    nombreCompleto: string;
    email: string;
    rol: RolUsuario;
    empresaId: number;
    empresaNombre: string;
}

// ============ Usuario ============
export interface UsuarioResumido {
    id: number;
    nombreCompleto: string;
    email: string;
}

// ============ Proyecto ============
export interface Proyecto {
    id: number;
    empresaId: number;
    empresaNombre: string;
    titulo: string;
    descripcion?: string;
    fechaInicio: string;
    fechaFinEstimada?: string;
    liderId?: number;
    liderNombre?: string;
}

export interface ProyectoCreateRequest {
    titulo: string;
    descripcion?: string;
    fechaFinEstimada?: string;
    liderId?: number;
}

// ============ Tarea ============
export interface Tarea {
    id: number;
    empresaId: number;
    proyectoId: number;
    proyectoTitulo: string;
    titulo: string;
    descripcion?: string;
    estado: EstadoTarea;
    prioridad: Prioridad;
    requiereRevision: boolean;
    tiempoEstimado?: number;
    tiempoReal: number;
    fechaLimite?: string;
    fechaBloqueo?: string;
    creador?: UsuarioResumido;
    usuarioAsignado?: UsuarioResumido;
    createdAt: string;
    updatedAt: string;
    totalEntregables: number;
    totalComentarios: number;
}

export interface TareaCreateRequest {
    proyectoId: number;
    titulo: string;
    descripcion?: string;
    tiempoEstimado?: number;
    fechaLimite?: string;
    prioridad?: Prioridad;
    requiereRevision?: boolean;
}

export interface AsignacionRequest {
    usuarioAsignadoId: number;
    prioridad?: Prioridad;
    fechaLimite?: string;
}

export interface CambioEstadoRequest {
    accion: 'ACEPTAR' | 'RECHAZAR';
    motivo?: string;
}

export interface MoverEstadoRequest {
    estadoDestino: EstadoTarea;
    comentario?: string;
}

export interface DecisionRevisionRequest {
    aprobado: boolean;
    comentario?: string;
}