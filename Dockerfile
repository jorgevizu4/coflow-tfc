# =============================================================================
# Stage 1 – Frontend build
# =============================================================================
FROM node:20-alpine AS frontend-build
WORKDIR /app
COPY frontend-web/package*.json ./
RUN npm ci
COPY frontend-web/ .
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# =============================================================================
# Stage 2 – Backend build
# =============================================================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS backend-build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline -q
COPY backend/src ./src
RUN mvn clean package -DskipTests -q

# =============================================================================
# Stage 3 – Runtime (all-in-one)
# =============================================================================
FROM ubuntu:22.04

ARG MYSQL_ROOT_PASSWORD=root
ARG MYSQL_DATABASE=coflow
ARG JWT_SECRET_KEY=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

ENV MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
ENV MYSQL_DATABASE=${MYSQL_DATABASE}
ENV JWT_SECRET_KEY=${JWT_SECRET_KEY}
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jre-headless \
    nginx \
    mysql-server \
    supervisor \
    && rm -rf /var/lib/apt/lists/* \
    # apt pre-initializes /var/lib/mysql; wipe it so our entrypoint runs --initialize-insecure cleanly
    && rm -rf /var/lib/mysql \
    && mkdir -p /var/lib/mysql /var/run/mysqld \
    && chown -R mysql:mysql /var/lib/mysql /var/run/mysqld

# ── Frontend ──────────────────────────────────────────────────────────────────
COPY --from=frontend-build /app/dist /usr/share/nginx/html
COPY frontend-web/nginx.conf /etc/nginx/conf.d/default.conf
RUN rm -f /etc/nginx/sites-enabled/default

# ── Backend ───────────────────────────────────────────────────────────────────
COPY --from=backend-build /app/target/*.jar /app/backend.jar

# ── Database init SQL ─────────────────────────────────────────────────────────
COPY database/coflow.sql /docker-entrypoint-initdb.d/coflow.sql

# ── Supervisor config (only [program:*] sections – [supervisord] lives in the main config) ──
RUN mkdir -p /var/log/supervisor && cat > /etc/supervisor/conf.d/coflow.conf << 'EOF'
[program:mysql]
command=/usr/sbin/mysqld --user=mysql
autostart=true
autorestart=true
priority=10
stdout_logfile=/var/log/supervisor/mysql.log
stderr_logfile=/var/log/supervisor/mysql.log

[program:backend]
; Wait for MySQL to be accepting connections before starting Spring Boot
command=/bin/bash -c "until mysqladmin -u root -p\"${MYSQL_ROOT_PASSWORD}\" -h 127.0.0.1 ping --silent 2>/dev/null; do sleep 2; done && exec java -jar /app/backend.jar"
autostart=true
autorestart=true
priority=20
environment=SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/%(ENV_MYSQL_DATABASE)s?useSSL=false&allowPublicKeyRetrieval=true",SPRING_DATASOURCE_USERNAME="root",SPRING_DATASOURCE_PASSWORD="%(ENV_MYSQL_ROOT_PASSWORD)s",JWT_SECRET_KEY="%(ENV_JWT_SECRET_KEY)s"
stdout_logfile=/var/log/supervisor/backend.log
stderr_logfile=/var/log/supervisor/backend.log

[program:nginx]
command=/usr/sbin/nginx -g "daemon off;"
autostart=true
autorestart=true
priority=30
stdout_logfile=/var/log/supervisor/nginx.log
stderr_logfile=/var/log/supervisor/nginx.log
EOF

# ── Entrypoint ────────────────────────────────────────────────────────────────
RUN cat > /entrypoint.sh << 'EOF'
#!/bin/bash
set -e

# Recreate socket dir in case /var/run is a tmpfs on this host
mkdir -p /var/run/mysqld
chown mysql:mysql /var/run/mysqld

if [ ! -d /var/lib/mysql/coflow ]; then
    echo "[init] Initializing MySQL data directory..."
    mysqld --initialize-insecure --user=mysql

    # Start mysqld in the background; redirect output so errors are visible
    mysqld --user=mysql > /tmp/mysqld-init.log 2>&1 &
    MYSQLD_PID=$!

    # Wait for the socket to be ready
    until mysqladmin -u root --socket=/var/run/mysqld/mysqld.sock ping --silent 2>/dev/null; do
        sleep 1
    done

    # Set root password and create a TCP-accessible user ('root'@'%')
    # so Spring Boot can connect via 127.0.0.1 (TCP, not socket)
    mysql -u root --socket=/var/run/mysqld/mysqld.sock -e "
        ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';
        CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';
        GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
        FLUSH PRIVILEGES;
        CREATE DATABASE IF NOT EXISTS \`${MYSQL_DATABASE}\`;
    "

    mysql -u root -p"${MYSQL_ROOT_PASSWORD}" --socket=/var/run/mysqld/mysqld.sock "${MYSQL_DATABASE}" \
        < /docker-entrypoint-initdb.d/coflow.sql

    mysqladmin -u root -p"${MYSQL_ROOT_PASSWORD}" --socket=/var/run/mysqld/mysqld.sock shutdown
    wait $MYSQLD_PID 2>/dev/null || true
    echo "[init] MySQL initialized."
fi

exec /usr/bin/supervisord -n -c /etc/supervisor/supervisord.conf
EOF
RUN chmod +x /entrypoint.sh

EXPOSE 80

ENTRYPOINT ["/entrypoint.sh"]
