# CoFlow

Gestor de proyectos colaborativo multi-tenant desarrollado como TFG.

- **Backend:** Spring Boot 3.2 · Java 17 · MySQL 8 · JWT
- **Frontend Web:** React 18 · Vite · TypeScript · Bootstrap 5
- **Mobile:** React Native · Expo · TypeScript

---

## Requisitos previos

| Herramienta | Versión mínima | Descarga |
|---|---|---|
| Java JDK | 17 | https://adoptium.net |
| Maven | 3.9 | https://maven.apache.org/download.cgi |
| Node.js | 20 | https://nodejs.org |
| MySQL | 8.0 | https://dev.mysql.com/downloads |
| Expo CLI | latest | `npm install -g expo-cli` |
| Expo Go (opcional) | latest | App Store / Google Play |

---

## Inicio rápido

### Con Docker (Recomendado)

Si tienes **Docker** y **Docker Compose** instalados, puedes levantar todo el entorno (Base de Datos + Backend + Frontend) con un solo comando:

1. **Clonar:**
   ```bash
   git clone https://github.com/jorgevizu4/coflow-tfc.git
   cd coflow-tfc
   ```

2. **Levantar los servicios:**
   ```bash
   docker-compose up -d --build
   ```

*   **Frontend:** [http://localhost:5173](http://localhost:5173)
*   **Backend API:** [http://localhost:8081](http://localhost:8081)
*   **Base de Datos:** `localhost:3307` (externo) / `db-v2:3306` (interno)

---

### Instalación Manual (Desarrollo)

Si prefieres ejecutar los servicios de forma nativa en tu máquina:

#### 1. Preparar la base de datos
Conéctate a MySQL como root y ejecuta:
```sql
CREATE DATABASE coflow;
```
> El backend espera MySQL en el **puerto 3306**.

#### 2. Arrancar el backend
```bash
cd backend-v2
mvn spring-boot:run
```
La API queda disponible en [http://localhost:8081](http://localhost:8081).

#### 3. Arrancar el frontend web
Abre **una terminal nueva** y ejecuta:
```bash
cd frontend-web
npm install
npm run dev
```
La aplicación queda disponible en [http://localhost:5173](http://localhost:5173).

#### 4. Arrancar la app móvil
Abre **otra terminal nueva** y ejecuta:
```bash
cd coflow-mobile
npm install
npm start

```
Escanea el QR con la app **Expo Go** en tu dispositivo, o presiona `a` para Android o `i` para iOS (requiere emulador).

---

## Estructura del proyecto

```
coflow-tfc/
├── backend-v2/              # API REST (Spring Boot + MySQL)
│   ├── src/main/java/       # Código fuente
│   ├── src/main/resources/  # application.properties
│   └── pom.xml
├── frontend-web/            # SPA React + Vite
│   ├── src/
│   └── package.json
└── coflow-mobile/           # App móvil React Native + Expo
    ├── src/
    │   ├── auth/            # Contexto de autenticación
    │   ├── components/      # Componentes reutilizables
    │   ├── navigation/      # Navegación (React Navigation)
    │   ├── screens/         # Pantallas de la app
    │   ├── services/        # Llamadas a la API
    │   └── types/           # Tipos TypeScript
    ├── App.tsx
    └── package.json
```

---

## Variables de entorno

El backend admite sobrescribir la configuración con variables de entorno. Las más relevantes:

| Variable | Por defecto (dev) | Descripción |
|---|---|---|
| `JWT_SECRET_KEY` | valor hex embebido en `application.properties` | Clave secreta HMAC-SHA256 para firmar tokens |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/coflow` | URL de conexión a MySQL |

Para el frontend web, crea el archivo `frontend-web/.env.local` si necesitas apuntar a un backend distinto al local:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Para la app móvil, edita `coflow-mobile/src/services/apiClient.ts` y ajusta la `BASE_URL` si el backend no está en la IP/puerto por defecto.

---

## Scripts disponibles

### Backend

| Comando | Descripción |
|---|---|
| `mvn spring-boot:run` | Arranca en modo desarrollo |
| `mvn package -DskipTests` | Genera el JAR en `target/` |
| `mvn test` | Ejecuta los tests (requiere la BD levantada) |

### Frontend Web

| Comando | Descripción |
|---|---|
| `npm run dev` | Servidor de desarrollo con HMR |
| `npm run build` | Compilación de producción en `dist/` |
| `npm run preview` | Vista previa del build de producción |

### App Móvil

| Comando | Descripción |
|---|---|
| `npm start` | Inicia el servidor Expo (Metro Bundler) |
| `npm run android` | Abre directamente en emulador/dispositivo Android |
| `npm run ios` | Abre directamente en simulador/dispositivo iOS |

---

## Usuario de prueba

Una vez arrancado el sistema, regístrate desde http://localhost:5173/signup para crear tu primera empresa y cuenta de administrador.

Los datos de prueba incluidos en los tests (perfil `test`) son:

| Email | Contraseña | Rol |
|---|---|---|
| admin@techcorp.com | password123 | ADMIN |
| maria.garcia@techcorp.com | password123 | LIDER |
| carlos.lopez@techcorp.com | password123 | REVISOR |
| ana.martinez@techcorp.com | password123 | USER |

> Estos usuarios **solo existen si ejecutas los scripts SQL de test** (`backend/src/test/resources/init.sql`).
