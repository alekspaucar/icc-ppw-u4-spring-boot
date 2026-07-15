# ============================================
# ETAPA 1: BUILD
# ============================================
FROM eclipse-temurin:25-jdk-jammy AS builder

WORKDIR /build

# Copiar archivos Gradle primero (aprovecha cache de Docker)
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Descargar dependencias (cacheado si build.gradle no cambia)
RUN ./gradlew dependencies --no-daemon

# Copiar codigo fuente
COPY src ./src

# Compilar JAR (omitir tests)
RUN ./gradlew build -x test --no-daemon

# ============================================
# ETAPA 2: RUNTIME
# ============================================
FROM eclipse-temurin:25-jdk-jammy

# Crear usuario no-root
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copiar solo el JAR desde la etapa de build
COPY --from=builder /build/build/libs/fundamentos01.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

# Health check (context-path = /api)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Xms256m", \
    "-Xmx512m", \
    "-jar", \
    "app.jar"]
