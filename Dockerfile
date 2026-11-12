# =====================================================================
# AlertaBairro API - Dockerfile multi-stage para o Render
# Etapa 1: build com Maven + JDK 21
# Etapa 2: runtime enxuto com JRE 21
# =====================================================================

# ---------- Etapa 1: build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# baixa as dependencias primeiro (aproveita o cache de camadas)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# compila e empacota (os testes rodam localmente com ./mvnw test)
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

ENV TZ=America/Sao_Paulo \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Duser.timezone=America/Sao_Paulo"

# usuario sem privilegios
RUN useradd --system --uid 1001 alertabairro
COPY --from=build /app/target/alertabairro-api.jar app.jar
USER alertabairro

# o Render injeta a porta na variavel PORT (lida em server.port=${PORT:8080})
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
