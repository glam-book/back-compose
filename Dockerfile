# Use the GraalVM 21 base image
FROM ghcr.io/graalvm/graalvm-community:21 as graalvm

# Set environment variables
ENV GRAALVM_HOME=/opt/graalvm-ce-java21
ENV PATH="$GRAALVM_HOME/bin:$PATH"

# Optional: Install native-image (if you plan to create native binaries)
# RUN gu install native-image

# Create a working directory
WORKDIR /app

# Copy Gradle build files
COPY build.gradle settings.gradle /app/

# Copy the gradle wrapper to avoid needing to install gradle globally
COPY gradlew /app/
COPY gradle /app/gradle

# Download dependencies (this step helps leverage Docker's cache)
RUN ./gradlew --no-daemon dependencies

# Copy the entire project
COPY . /app

# Build the application using Gradle
RUN ./gradlew --no-daemon build

# Expose the port your application uses (change as needed)
EXPOSE 8080

# Run the application (for standard JVM execution)
CMD ["./gradlew", "bootRun"]

# Optional: native build
#RUN ./gradlew nativeCompile
#CMD ["./build/native/nativeCompile/your-app"]
