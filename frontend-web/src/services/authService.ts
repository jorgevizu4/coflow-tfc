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
            const error = await response.json();
            throw new Error(error.message || "Error en el inicio de sesión");
        }

        const data: ApiResponse<LoginResponse> = await response.json();
        return data.data;
    },

    async signup(
        nombreEmpresa: string,
        nombreAdministrador: string,
        emailAdministrador: string,
        passwordAdministrador: string,
        passwordRepeat: string
    ): Promise<LoginResponse> {
        if (passwordAdministrador !== passwordRepeat) {
            throw new Error("Las contraseñas no coinciden");
        }

        const response = await fetch(`${API_BASE_URL}/auth/register-company`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                nombreEmpresa,
                nombreAdministrador,
                emailAdministrador,
                passwordAdministrador,
            }),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || "Error en el registro");
        }

        const data: ApiResponse<LoginResponse> = await response.json();
        return data.data;
    },

    getStoredToken(): string | null {
        return localStorage.getItem("token");
    },

    saveToken(token: string) {
        localStorage.setItem("token", token);
    },

    clearToken() {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
    },

    saveUser(user: LoginResponse) {
        localStorage.setItem("user", JSON.stringify(user));
    },

    getSavedUser(): LoginResponse | null {
        const user = localStorage.getItem("user");
        return user ? JSON.parse(user) : null;
    },
};
