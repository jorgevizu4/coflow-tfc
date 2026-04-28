# --------- STAGE 1: Build ---------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiamos pom primero para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente
COPY src ./src

# Construimos el JAR
RUN mvn clean package -DskipTests

# --------- STAGE 2: Runtime ---------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copiamos el jar generado
COPY --from=builder /app/target/*.jar app.jar

# Puerto del backend
EXPOSE 8081

# Ejecutar aplicación
ENTRYPOINT ["java","-jar","app.jar"]