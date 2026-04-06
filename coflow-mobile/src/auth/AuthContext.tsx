import React, { createContext, useContext, useState, useEffect } from 'react';
import * as SecureStore from 'expo-secure-store';
import { LoginResponse } from '../types/types';

// ─── CONFIGURACIÓN DE LA API ──────────────────────────────────────────────────
//
// CONEXIÓN A LA NUBE (teórica):
// En producción, esta URL apuntaría a tu backend desplegado en la nube,
// por ejemplo en AWS, Azure, Railway, Render, etc.
//
// Ejemplos de URLs en la nube:
//   https://api.coflow.app/api/v1          ← dominio propio
//   https://coflow-api.railway.app/api/v1  ← Railway
//   https://coflow-xyz.onrender.com/api/v1 ← Render
//
// Para desarrollo local (con el docker-compose del proyecto):
//   Android emulator → http://10.0.2.2:8080/api/v1
//   iOS simulator    → http://localhost:8080/api/v1
//   Dispositivo físico → http://TU_IP_LOCAL:8080/api/v1
//
export const API_BASE_URL = 'https://api.coflow.app/api/v1'; // ← CAMBIAR por tu URL real

const TOKEN_KEY = 'coflow_token';
const USER_KEY = 'coflow_user';

interface AuthContextType {
  user: LoginResponse | null;
  token: string | null;
  login: (user: LoginResponse) => Promise<void>;
  logout: () => Promise<void>;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Recuperar sesión guardada al arrancar la app
    (async () => {
      try {
        const savedToken = await SecureStore.getItemAsync(TOKEN_KEY);
        const savedUser = await SecureStore.getItemAsync(USER_KEY);
        if (savedToken && savedUser) {
          setToken(savedToken);
          setUser(JSON.parse(savedUser));
        }
      } catch {
        // Si falla SecureStore, continuar sin sesión
      } finally {
        setIsLoading(false);
      }
    })();
  }, []);

  const login = async (userData: LoginResponse) => {
    await SecureStore.setItemAsync(TOKEN_KEY, userData.token);
    await SecureStore.setItemAsync(USER_KEY, JSON.stringify(userData));
    setToken(userData.token);
    setUser(userData);
  };

  const logout = async () => {
    await SecureStore.deleteItemAsync(TOKEN_KEY);
    await SecureStore.deleteItemAsync(USER_KEY);
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return ctx;
}
