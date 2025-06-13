# ========== Stage 1: Build ==========
FROM ghcr.io/graalvm/graalvm-community:21 as builder

ENV GRAALVM_HOME=/opt/graalvm-ce-java21
ENV PATH="$GRAALVM_HOME/bin:$PATH"

WORKDIR /app

# Копируем gradle wrapper + зависимости
COPY gradlew settings.gradle build.gradle gradle /app/

# Создаём папку для gradle-кеша
RUN mkdir -p /home/gradle/.gradle && \
    chmod -R 777 /home/gradle && \
    chown -R root:root /home/gradle

ENV GRADLE_USER_HOME=/home/gradle/.gradle

# Добавляем пустой .credentials (будет заменён при билде)
COPY .credentials /app/.credentials

# Кэшируем зависимости (если поменяется build.gradle — этот слой обновится)
RUN ./gradlew --no-daemon dependencies || true

# Копируем остальной код
COPY . /app

# Финальная сборка
RUN ./gradlew --no-daemon build

# ========== Stage 2: Run ==========
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Копируем собранный .jar
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Порт, который слушает приложение
EXPOSE 8080

# Запуск
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Optional: native build
#RUN ./gradlew nativeCompile
#CMD ["./build/native/nativeCompile/your-app"]
