import { API_BASE_URL } from "../auth/constants";
import { ApiResponse, LoginResponse } from "../types/types";

// Re-export for backwards-compatibility with existing imports
export type { LoginResponse };

export const authService = {
    async login(email: string, password: string): Promise<LoginResponse> {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) {
            try {
                const error = await response.json();
                throw new Error(error.message || "Credenciales incorrectas");
            } catch {
                throw new Error("Credenciales incorrectas");
            }
        }

        const data: ApiResponse<LoginResponse> = await response.json();
        return data.data;
    },

    async signup(
        empresaId: number,
        nombre: string,
        apellidos: string,
        email: string,
        password: string,
        passwordRepeat: string,
        rol: string
    ): Promise<LoginResponse> {
        if (password !== passwordRepeat) {
            throw new Error("Las contraseñas no coinciden");
        }

        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ empresaId, nombre, apellidos, email, password, rol }),
        });

        if (!response.ok) {
            try {
                const error = await response.json();
                throw new Error(error.message || "Error en el registro");
            } catch {
                throw new Error("Error en el registro");
            }
        }

        const data: ApiResponse<LoginResponse> = await response.json();
        return data.data;
    },

    getStoredToken(): string | null {
        return sessionStorage.getItem("token");
    },

    saveToken(token: string) {
        sessionStorage.setItem("token", token);
    },

    clearToken() {
        sessionStorage.removeItem("token");
        sessionStorage.removeItem("user");
        localStorage.removeItem("token");
        localStorage.removeItem("user");
    },

    saveUser(user: LoginResponse) {
        sessionStorage.setItem("user", JSON.stringify(user));
    },

    getSavedUser(): LoginResponse | null {
        const user = sessionStorage.getItem("user");
        return user ? JSON.parse(user) : null;
    },
};
