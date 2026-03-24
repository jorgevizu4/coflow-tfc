import { apiClient } from './apiClient';
import {
    ApiResponse,
    Tarea,
    TareaCreateRequest,
    AsignacionRequest,
    CambioEstadoRequest,
    MoverEstadoRequest,
    DecisionRevisionRequest,
    EstadoTarea,
    Prioridad,
} from '../types/types';

interface ListarTareasParams {
    estado?: EstadoTarea;
    prioridad?: Prioridad;
    proyectoId?: number;
    usuarioAsignadoId?: number;
}

export const tareaService = {
    listar(params?: ListarTareasParams): Promise<ApiResponse<Tarea[]>> {
        const query = new URLSearchParams();
        if (params?.estado) query.set('estado', params.estado);
        if (params?.prioridad) query.set('prioridad', params.prioridad);
        if (params?.proyectoId) query.set('proyectoId', String(params.proyectoId));
        if (params?.usuarioAsignadoId) query.set('usuarioAsignadoId', String(params.usuarioAsignadoId));
        const qs = query.toString();
        return apiClient.get<ApiResponse<Tarea[]>>(`/tareas${qs ? '?' + qs : ''}`);
    },

    obtener(id: number): Promise<ApiResponse<Tarea>> {
        return apiClient.get<ApiResponse<Tarea>>(`/tareas/${id}`);
    },

    listarPorProyecto(proyectoId: number): Promise<ApiResponse<Tarea[]>> {
        return apiClient.get<ApiResponse<Tarea[]>>(`/tareas/proyecto/${proyectoId}`);
    },

    misTareas(): Promise<ApiResponse<Tarea[]>> {
        return apiClient.get<ApiResponse<Tarea[]>>('/tareas/mis-tareas');
    },

    pendientesRevision(): Promise<ApiResponse<Tarea[]>> {
        return apiClient.get<ApiResponse<Tarea[]>>('/tareas/pendientes-revision');
    },

    seguimiento(): Promise<ApiResponse<Record<string, unknown>>> {
        return apiClient.get<ApiResponse<Record<string, unknown>>>('/tareas/seguimiento');
    },

    crear(dto: TareaCreateRequest): Promise<ApiResponse<Tarea>> {
        return apiClient.post<ApiResponse<Tarea>>('/tareas', dto);
    },

    asignar(id: number, dto: AsignacionRequest): Promise<ApiResponse<Tarea>> {
        return apiClient.patch<ApiResponse<Tarea>>(`/tareas/${id}/asignar`, dto);
    },

    cambiarEstado(id: number, dto: CambioEstadoRequest): Promise<ApiResponse<Tarea>> {
        return apiClient.patch<ApiResponse<Tarea>>(`/tareas/${id}/estado`, dto);
    },

    moverEstado(id: number, dto: MoverEstadoRequest): Promise<ApiResponse<Tarea>> {
        return apiClient.patch<ApiResponse<Tarea>>(`/tareas/${id}/mover`, dto);
    },

    decidirRevision(id: number, dto: DecisionRevisionRequest): Promise<ApiResponse<Tarea>> {
        return apiClient.post<ApiResponse<Tarea>>(`/tareas/${id}/decision`, dto);
    },

    eliminar(id: number): Promise<ApiResponse<void>> {
        return apiClient.delete<ApiResponse<void>>(`/tareas/${id}`);
    },
};
