# Dockerfile para la aplicación Spring Boot

# Etapa 1: Construcción
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN gradle clean build -x test --no-daemon

# Etapa 2: Ejecución
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar el JAR construido desde la etapa de build
COPY --from=build /app/build/libs/*.jar app.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]

