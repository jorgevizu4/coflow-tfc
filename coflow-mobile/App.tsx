import React, { useEffect } from 'react';
import { StatusBar } from 'expo-status-bar';
import { AuthProvider, useAuth } from './src/auth/AuthContext';
import AppNavigator from './src/navigation/AppNavigator';
import { setApiToken } from './src/services/apiClient';

// Sincroniza el token con el apiClient cuando la sesión cambia
function TokenSync() {
  const { token } = useAuth();
  useEffect(() => {
    setApiToken(token);
  }, [token]);
  return null;
}

export default function App() {
  return (
    <AuthProvider>
      <TokenSync />
      <StatusBar style="light" />
      <AppNavigator />
    </AuthProvider>
  );
}
