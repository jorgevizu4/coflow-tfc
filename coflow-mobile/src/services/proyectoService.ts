import { apiClient } from './apiClient';
import { ApiResponse, Proyecto, ProyectoCreateRequest, UsuarioResumido } from '../types/types';

export const proyectoService = {
  listar: () => apiClient.get<ApiResponse<Proyecto[]>>('/proyectos'),
  crear: (dto: ProyectoCreateRequest) =>
    apiClient.post<ApiResponse<Proyecto>>('/proyectos', dto),
  getMiembros: (proyectoId: number) =>
    apiClient.get<ApiResponse<UsuarioResumido[]>>(`/proyectos/${proyectoId}/miembros`),
};
