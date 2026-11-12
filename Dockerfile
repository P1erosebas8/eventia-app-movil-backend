# ==========================================
# Etapa 1: Compilación y Construcción (Builder)
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar configuración de Maven Wrapper
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Dar permisos de ejecución a mvnw (crucial para entornos Unix / Docker)
RUN chmod +x mvnw

# Descargar dependencias de Maven en caché para acelerar futuros builds
RUN ./mvnw dependency:go-offline -B || true

# Copiar el código fuente de la aplicación
COPY src ./src

# Compilar el empaquetado JAR optimizado sin pruebas unitarias
RUN ./mvnw clean package -DskipTests

# ==========================================
# Etapa 2: Imagen Final Ligera (Runtime)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario seguro sin privilegios root
RUN addgroup -S eventiagroup && adduser -S eventiauser -G eventiagroup
USER eventiauser

# Copiar el archivo ejecutable JAR compilado en la etapa previa
COPY --from=builder --chown=eventiauser:eventiagroup /app/target/*.jar app.jar

# Render define el puerto dinámicamente mediante la variable de entorno $PORT
ENV PORT=8080
EXPOSE ${PORT}

# Parámetros optimizados para Render:
# - UseContainerSupport y MaxRAMPercentage=75.0 evitan que la JVM sobrepase los 512MB de RAM en Render Free/Starter.
# - Dserver.port vincula Tomcat directamente al puerto asignado por Render.
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-8080} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

