# Use an official OpenJDK image to run the application
FROM eclipse-temurin:21-jdk

# Set the working directory
WORKDIR /app

# Copy the built application from the build stage
COPY build/libs/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
# Otel java agent와 Properties 파일이 같은 디렉토리에 있어야 합니다.
ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-javaagent:./opentelemetry-javaagent.jar", "-Dotel.javaagent.configuration-file=otel-dev.properties", "-jar", "app.jar" ]