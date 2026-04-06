import { API_BASE_URL } from '../auth/AuthContext';
import { ApiResponse, LoginResponse } from '../types/types';

export const authService = {
  async login(email: string, password: string): Promise<LoginResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Credenciales incorrectas');
    }

    const data: ApiResponse<LoginResponse> = await response.json();
    return data.data;
  },

  async signup(
    empresaNombre: string,
    nombre: string,
    apellidos: string,
    email: string,
    password: string
  ): Promise<LoginResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/register-empresa`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ empresaNombre, nombre, apellidos, email, password }),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Error en el registro');
    }

    const data: ApiResponse<LoginResponse> = await response.json();
    return data.data;
  },
};
