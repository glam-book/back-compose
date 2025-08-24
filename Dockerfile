FROM ghcr.io/graalvm/graalvm-community:21 as builder

ENV GRAALVM_HOME=/opt/graalvm-ce-java21
ENV PATH="$GRAALVM_HOME/bin:$PATH"

WORKDIR /app

COPY gradlew settings.gradle build.gradle gradle /app/
RUN mkdir -p /home/gradle/.gradle && \
    chmod -R 777 /home/gradle && \
    chown -R root:root /home/gradle
ENV GRADLE_USER_HOME=/home/gradle/.gradle
RUN ./gradlew --no-daemon dependencies || true
COPY . /app
ARG GLAM_TG_BOT_TOKEN
ENV GLAM_TG_BOT_TOKEN=$GLAM_TG_BOT_TOKEN
RUN ./gradlew --no-daemon bootJar

# ========== Stage 2: Run ==========
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Копируем всё из builder (включая gradlew, исходники и changelog)
COPY --from=builder /app /app

EXPOSE 8080

# Запуск: сначала liquibaseUpdate, потом приложение
ENTRYPOINT ["sh", "-c", "./gradlew --no-daemon update && java -jar /app/build/libs/*.jar"]