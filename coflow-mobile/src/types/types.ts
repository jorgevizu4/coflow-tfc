export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export type EstadoTarea =
  | 'PENDIENTE' | 'ASIGNADA' | 'EN_PROCESO' | 'BLOQUEADA'
  | 'EN_REVISION' | 'APROBADA' | 'RECHAZADA' | 'COMPLETADA';

export type Prioridad = 'BAJA' | 'MEDIA' | 'ALTA' | 'URGENTE';
export type RolUsuario = 'ADMIN' | 'LIDER' | 'REVISOR' | 'USER';

export interface LoginResponse {
  token: string;
  usuarioId: number;
  nombreCompleto: string;
  email: string;
  rol: RolUsuario;
  empresaId: number;
  empresaNombre: string;
}

export interface UsuarioResumido {
  id: number;
  nombreCompleto: string;
  email: string;
}

export interface Proyecto {
  id: number;
  empresaId: number;
  empresaNombre: string;
  titulo: string;
  descripcion?: string;
  fechaInicio: string;
  fechaFinEstimada?: string;
  liderNombre?: string;
}

export interface Tarea {
  id: number;
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
  prioridad?: Prioridad;
  fechaLimite?: string;
  requiereRevision?: boolean;
}

export interface Comentario {
  id: number;
  contenido: string;
  fechaCreacion: string;
  autorId: number;
  autorNombre: string;
}
