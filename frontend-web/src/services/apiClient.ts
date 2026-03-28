import { API_BASE_URL } from "../auth/constants";
import { authService } from "./authService";

interface RequestOptions extends RequestInit {
    headers?: Record<string, string>;
}

function isTokenExpired(token: string): boolean {
    try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        return Date.now() / 1000 > payload.exp;
    } catch {
        return true;
    }
}

export const apiClient = {
    async request<T>(
        endpoint: string,
        options: RequestOptions = {}
    ): Promise<T> {
        const token = authService.getStoredToken();

        const headers: Record<string, string> = {
            "Content-Type": "application/json",
            ...options.headers,
        };

        if (token) {
            if (isTokenExpired(token)) {
                authService.clearToken();
                window.location.href = "/login";
                throw new Error("Sesión expirada. Por favor, inicia sesión nuevamente.");
            }
            headers["Authorization"] = `Bearer ${token}`;
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers,
        });

        // Si recibe 401 o 403, el token expiró o es inválido
        if (response.status === 401 || response.status === 403) {
            authService.clearToken();
            window.location.href = "/login";
            throw new Error("Sesión expirada. Por favor, inicia sesión nuevamente.");
        }

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `Error: ${response.statusText}`);
        }

        return response.json();
    },

    get<T>(endpoint: string) {
        return apiClient.request<T>(endpoint, { method: "GET" });
    },

    post<T>(endpoint: string, body?: any) {
        return apiClient.request<T>(endpoint, {
            method: "POST",
            body: JSON.stringify(body),
        });
    },

    put<T>(endpoint: string, body?: any) {
        return apiClient.request<T>(endpoint, {
            method: "PUT",
            body: JSON.stringify(body),
        });
    },

    patch<T>(endpoint: string, body?: unknown) {
        return apiClient.request<T>(endpoint, {
            method: "PATCH",
            body: JSON.stringify(body),
        });
    },

    delete<T>(endpoint: string) {
        return apiClient.request<T>(endpoint, { method: "DELETE" });
    },
};
