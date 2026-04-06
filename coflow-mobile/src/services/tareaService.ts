import { apiClient } from './apiClient';
import { ApiResponse, Tarea, TareaCreateRequest, Comentario } from '../types/types';

export const tareaService = {
  listar: () => apiClient.get<ApiResponse<Tarea[]>>('/tareas'),
  misTareas: () => apiClient.get<ApiResponse<Tarea[]>>('/tareas/mis-tareas'),
  obtener: (id: number) => apiClient.get<ApiResponse<Tarea>>(`/tareas/${id}`),
  listarPorProyecto: (proyectoId: number) =>
    apiClient.get<ApiResponse<Tarea[]>>(`/tareas/proyecto/${proyectoId}`),
  crear: (dto: TareaCreateRequest) =>
    apiClient.post<ApiResponse<Tarea>>('/tareas', dto),
  cambiarEstado: (id: number, accion: 'ACEPTAR' | 'RECHAZAR') =>
    apiClient.patch<ApiResponse<Tarea>>(`/tareas/${id}/estado`, { accion }),
  moverEstado: (id: number, estadoDestino: string) =>
    apiClient.patch<ApiResponse<Tarea>>(`/tareas/${id}/mover`, { estadoDestino }),
  eliminar: (id: number) =>
    apiClient.delete<ApiResponse<void>>(`/tareas/${id}`),
};

export const comentarioService = {
  listarPorTarea: (tareaId: number) =>
    apiClient.get<ApiResponse<Comentario[]>>(`/comentarios/tarea/${tareaId}`),
  crear: (tareaId: number, contenido: string) =>
    apiClient.post<ApiResponse<Comentario>>('/comentarios', { tareaId, contenido }),
  eliminar: (id: number) =>
    apiClient.delete<ApiResponse<void>>(`/comentarios/${id}`),
};
