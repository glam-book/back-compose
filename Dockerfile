# syntax=docker/dockerfile:1.4
ARG JDK_VERSION=17
ARG GLAM_TG_BOT_TOKEN

FROM gradle:8.4-jdk${JDK_VERSION} AS builder
WORKDIR /home/gradle/project
ENV GLAM_TG_BOT_TOKEN=$GLAM_TG_BOT_TOKEN

# Copy gradle wrapper and build files first to leverage layer caching
COPY gradle gradle
COPY gradlew .
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew

# Resolve dependencies using BuildKit cache mount (will populate the cache on first build)
RUN --mount=type=cache,target=/home/gradle/.gradle ./gradlew --no-daemon dependencies || true

# Copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/home/gradle/.gradle ./gradlew build -x test --no-daemon

FROM eclipse-temurin:${JDK_VERSION}-jre-alpine AS runtime
WORKDIR /app
EXPOSE 8080
# Copy built jar from builder stage (assumes Gradle produces a single fat/boot jar in build/libs)
COPY --from=builder /home/gradle/project/build/libs/*.jar ./app.jar

# Запуск: сначала liquibaseUpdate, потом приложение
ENTRYPOINT ["sh", "-c", "./gradlew --no-daemon update && java -jar /app/build/libs/*.jar"]
