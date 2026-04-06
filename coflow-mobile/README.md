# CoFlow Mobile 📱

App móvil para CoFlow, desarrollada con **React Native + Expo**.
Replica la funcionalidad del frontend web y conecta con el mismo backend.

## Stack

- React Native 0.74
- Expo ~51
- TypeScript
- React Navigation (Stack + Bottom Tabs)
- Expo SecureStore (almacenamiento seguro del token JWT)

## Requisitos

- Node.js 20+
- Expo CLI: `npm install -g expo-cli`
- Para probar en móvil: app **Expo Go** en tu teléfono

## Instalación

```bash
cd coflow-mobile
npm install
npm start
```

Escanea el QR con Expo Go (Android) o la cámara (iOS).

---

## ☁️ Conexión a la nube (configuración)

La URL del backend se configura en **`src/auth/AuthContext.tsx`**:

```ts
export const API_BASE_URL = 'https://api.coflow.app/api/v1'; // ← cambia esto
```

### URLs según entorno

| Entorno | URL |
|---|---|
| Producción (nube) | `https://tu-dominio.com/api/v1` |
| Railway | `https://coflow-api.railway.app/api/v1` |
| Render | `https://coflow-xyz.onrender.com/api/v1` |
| Local (emulador Android) | `http://10.0.2.2:8080/api/v1` |
| Local (simulador iOS) | `http://localhost:8080/api/v1` |
| Local (dispositivo físico) | `http://TU_IP_LOCAL:8080/api/v1` |

> Para obtener tu IP local en Windows: `ipconfig` → busca "IPv4"

---

## Pantallas

| Pantalla | Descripción |
|---|---|
| Login | Inicio de sesión con JWT |
| Signup | Registro de nueva empresa + admin |
| Tareas | Lista de tareas con filtros por estado, acciones rápidas |
| Detalle de tarea | Info completa + comentarios en tiempo real |
| Proyectos | Lista de proyectos, crear nuevo |
| Perfil | Datos del usuario, info de conexión, cerrar sesión |

## Estructura

```
coflow-mobile/
├── App.tsx                  # Entrada principal
├── src/
│   ├── auth/
│   │   └── AuthContext.tsx  # Contexto auth + API_BASE_URL
│   ├── services/
│   │   ├── apiClient.ts     # Cliente HTTP con JWT
│   │   ├── authService.ts   # Login / Signup
│   │   ├── tareaService.ts  # CRUD tareas + comentarios
│   │   └── proyectoService.ts
│   ├── screens/
│   │   ├── LoginScreen.tsx
│   │   ├── SignupScreen.tsx
│   │   ├── TareasScreen.tsx
│   │   ├── DetalleTareaScreen.tsx
│   │   ├── ProyectosScreen.tsx
│   │   └── PerfilScreen.tsx
│   ├── navigation/
│   │   └── AppNavigator.tsx # Stack + Tabs
│   ├── components/
│   │   └── theme.ts         # Colores, badges, helpers
│   └── types/
│       └── types.ts         # Interfaces TypeScript (mismas que el web)
```
