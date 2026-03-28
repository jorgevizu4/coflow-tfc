import { apiClient } from './apiClient';
import { ApiResponse, Comentario, ComentarioCrearRequest } from '../types/types';

export const comentarioService = {
    listarPorTarea(tareaId: number): Promise<ApiResponse<Comentario[]>> {
        return apiClient.get<ApiResponse<Comentario[]>>(`/comentarios/tarea/${tareaId}`);
    },

    crear(req: ComentarioCrearRequest): Promise<ApiResponse<Comentario>> {
        return apiClient.post<ApiResponse<Comentario>>('/comentarios', req);
    },

    eliminar(id: number): Promise<ApiResponse<void>> {
        return apiClient.delete<ApiResponse<void>>(`/comentarios/${id}`);
    },
};
