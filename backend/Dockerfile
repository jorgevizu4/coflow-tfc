FROM maven:3.9.9-eclipse-temurin-23 AS builder

WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:23-jre-alpine

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=5 \
  CMD wget --quiet --spider http://localhost:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
