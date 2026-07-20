# --- Build stage: バックエンド(Maven)+フロントエンド(frontend-maven-plugin経由のVite)を1つのjarにビルド ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY frontend/package.json frontend/package-lock.json* ./frontend/
COPY src ./src
COPY frontend ./frontend

RUN mvn -B -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-Xmx256m -Xms128m"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
