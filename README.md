# CoFlow

Gestor de proyectos colaborativo multi-tenant desarrollado como TFG.

- **Backend:** Spring Boot 3.2 · Java 17 · PostgreSQL · JWT
- **Frontend:** React 18 · Vite · TypeScript · Bootstrap 5

---

## Requisitos previos

| Herramienta | Versión mínima | Descarga |
|---|---|---|
| Java JDK | 17 | https://adoptium.net |
| Maven | 3.9 | https://maven.apache.org/download.cgi |
| Node.js | 20 | https://nodejs.org |
| PostgreSQL | 15 | https://www.postgresql.org/download |

---

## Inicio rápido

### Con Docker (Recomendado)

Si tienes **Docker** y **Docker Compose** instalados, puedes levantar todo el entorno (Base de Datos + Backend + Frontend) con un solo comando:

1. **Clonar y cambiar a la rama de desarrollo:**
   ```bash
   git clone https://github.com/jorgevizu4/coflow-tfc.git
   cd coflow-tfc
   git checkout devmmc
   ```

2. **Levantar los servicios:**
   ```bash
   docker-compose up -d --build
   ```

*   **Frontend:** [http://localhost:5173](http://localhost:5173)
*   **Backend API:** [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
*   **Base de Datos:** `localhost:5433` (externo) / `db:5432` (interno)

---

### Instalación Manual (Desarrollo)

Si prefieres ejecutar los servicios de forma nativa en tu máquina:

#### 1. Preparar la base de datos
Conéctate a PostgreSQL como superusuario y ejecuta:
```sql
CREATE USER taskmanager WITH PASSWORD 'taskmanager123';
CREATE DATABASE taskmanager OWNER taskmanager;
```
> El backend espera PostgreSQL en el **puerto 5433**.

#### 2. Arrancar el backend
```bash
cd backend
mvn spring-boot:run
```
La API queda disponible en [http://localhost:8080/api/v1](http://localhost:8080/api/v1).

#### 3. Arrancar el frontend
Abre **una terminal nueva** y ejecuta:
```bash
cd frontend-web
npm install
npm run dev
```
La aplicación queda disponible en [http://localhost:5173](http://localhost:5173).

---

## Estructura del proyecto

```
coflow-tfc/
├── backend/                 # API REST (Spring Boot)
│   ├── src/main/java/       # Código fuente
│   ├── src/main/resources/  # application.yml
│   └── pom.xml
└── frontend-web/            # SPA React + Vite
    ├── src/
    └── package.json
```

---

## Variables de entorno

El backend admite sobrescribir la configuración con variables de entorno. Las más relevantes:

| Variable | Por defecto (dev) | Descripción |
|---|---|---|
| `JWT_SECRET` | valor largo embebido en `application.yml` | Clave secreta HMAC-SHA256 para firmar tokens |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:3001,http://localhost:5173` | Orígenes permitidos por CORS |

Para el frontend, crea el archivo `frontend-web/.env.local` si necesitas apuntar a un backend distinto al local:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

---

## Scripts disponibles

### Backend

| Comando | Descripción |
|---|---|
| `mvn spring-boot:run` | Arranca en modo desarrollo |
| `mvn package -DskipTests` | Genera el JAR en `target/` |
| `mvn test` | Ejecuta los tests (requiere la BD levantada) |

### Frontend

| Comando | Descripción |
|---|---|
| `npm run dev` | Servidor de desarrollo con HMR |
| `npm run build` | Compilación de producción en `dist/` |
| `npm run preview` | Vista previa del build de producción |

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
