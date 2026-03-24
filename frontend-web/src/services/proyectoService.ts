import { apiClient } from './apiClient';
import { ApiResponse, Proyecto, ProyectoCreateRequest } from '../types/types';

export const proyectoService = {
    listar(): Promise<ApiResponse<Proyecto[]>> {
        return apiClient.get<ApiResponse<Proyecto[]>>('/proyectos');
    },

    crear(dto: ProyectoCreateRequest): Promise<ApiResponse<Proyecto>> {
        return apiClient.post<ApiResponse<Proyecto>>('/proyectos', dto);
    },
};
