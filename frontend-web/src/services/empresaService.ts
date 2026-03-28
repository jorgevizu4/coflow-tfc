import { API_BASE_URL } from "../auth/constants";
import { ApiResponse } from "../types/types";

export interface EmpresaResumida {
    id: number;
    nombre: string;
}

export const empresaService = {
    async listar(): Promise<EmpresaResumida[]> {
        const response = await fetch(`${API_BASE_URL}/empresas`);
        if (!response.ok) throw new Error("Error al cargar empresas");
        const data: ApiResponse<EmpresaResumida[]> = await response.json();
        return data.data;
    },
};
