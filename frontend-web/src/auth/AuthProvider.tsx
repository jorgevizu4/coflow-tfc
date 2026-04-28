import { useContext, createContext, useEffect, useState } from "react";
import React from "react";
import { authService } from "../services/authService";
import { LoginResponse } from "../types/types";

interface AuthContextType {
    isAuthenticated: boolean;
    user: LoginResponse | null;
    token: string | null;
    login: (email: string, password: string) => Promise<void>;
    signup: (
        empresaId: number,
        nombre: string,
        apellidos: string,
        email: string,
        password: string,
        passwordRepeat: string,
        rol: string
    ) => Promise<void>;
    logout: () => void;
}

interface AuthProviderProps {
    children: React.ReactNode;
}

const AuthContext = createContext<AuthContextType>({
    isAuthenticated: false,
    user: null,
    token: null,
    login: async () => {},
    signup: async (_eid: number, _n: string, _a: string, _e: string, _p: string, _pr: string, _r: string) => {},
    logout: () => {},
});

export default function AuthProvider({ children }: AuthProviderProps) {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [user, setUser] = useState<LoginResponse | null>(null);
    const [token, setToken] = useState<string | null>(null);

    // Recuperar sesión al montar
    useEffect(() => {
        const stored = authService.getStoredToken();
        const savedUser = authService.getSavedUser();

        if (stored && savedUser) {
            setToken(stored);
            setUser(savedUser);
            setIsAuthenticated(true);
        }
    }, []);

    const login = async (email: string, password: string) => {
        try {
            const response = await authService.login(email, password);
            authService.saveToken(response.token);
            authService.saveUser(response);
            setUser(response);
            setToken(response.token);
            setIsAuthenticated(true);
        } catch (error) {
            throw error;
        }
    };

    const signup = async (
        empresaId: number,
        nombre: string,
        apellidos: string,
        email: string,
        password: string,
        passwordRepeat: string,
        rol: string
    ) => {
        if (password !== passwordRepeat) {
            throw new Error("Las contraseñas no coinciden");
        }
        try {
            const response = await authService.signup(
                empresaId,
                nombre,
                apellidos,
                email,
                password,
                passwordRepeat,
                rol
            );
            authService.saveToken(response.token);
            authService.saveUser(response);
            setUser(response);
            setToken(response.token);
            setIsAuthenticated(true);
        } catch (error) {
            throw error;
        }
    };

    const logout = () => {
        authService.clearToken();
        setUser(null);
        setToken(null);
        setIsAuthenticated(false);
    };

    return (
        <AuthContext.Provider
            value={{
                isAuthenticated,
                user,
                token,
                login,
                signup,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);

