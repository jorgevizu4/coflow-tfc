import { apiClient } from './apiClient';
import { ApiResponse, Proyecto, ProyectoCreateRequest, UsuarioResumido } from '../types/types';

export const proyectoService = {
    listar(): Promise<ApiResponse<Proyecto[]>> {
        return apiClient.get<ApiResponse<Proyecto[]>>('/proyectos');
    },

    crear(dto: ProyectoCreateRequest): Promise<ApiResponse<Proyecto>> {
        return apiClient.post<ApiResponse<Proyecto>>('/proyectos', dto);
    },

    getMiembros(proyectoId: number): Promise<ApiResponse<UsuarioResumido[]>> {
        return apiClient.get<ApiResponse<UsuarioResumido[]>>(`/proyectos/${proyectoId}/miembros`);
    },
};
