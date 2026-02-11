ARG JDK_VERSION=21

FROM gradle:8.11.1-jdk${JDK_VERSION} AS builder
WORKDIR /home/gradle/project

COPY gradle gradle
COPY gradlew .
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew

# Загружаем зависимости без cache mount, они сохранятся в образе
RUN ./gradlew --no-daemon dependencies || true

COPY src ./src
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:${JDK_VERSION}-jre-alpine AS runtime
WORKDIR /app
EXPOSE 8080

# Копируем jar
COPY --from=builder /home/gradle/project/build/libs/*.jar ./app.jar

# Копируем кэш Gradle (с дистрибутивом и зависимостями)
COPY --from=builder /home/gradle/.gradle /root/.gradle

# Копируем только то, что нужно для gradlew update (например, build.gradle, settings.gradle, liquibase-чанжлоги)
COPY --from=builder /home/gradle/project/build.gradle /home/gradle/project/settings.gradle /app/
COPY --from=builder /home/gradle/project/gradle /app/gradle
COPY --from=builder /home/gradle/project/gradlew /app/
COPY --from=builder /home/gradle/project/src/main/resources/db/changelog /app/src/main/resources/db/changelog

ENV GRADLE_USER_HOME=/root/.gradle

ENTRYPOINT ["sh", "-c", "./gradlew --no-daemon update && java $JAVA_OPTS -jar /app/app.jar"]