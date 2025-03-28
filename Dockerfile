# Use an official Gradle image to build the application
FROM gradle:7.5.1-jdk17 AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradle /app/gradle
COPY gradlew /app/gradlew
COPY build.gradle.kts /app/
COPY settings.gradle.kts /app/

# Copy the source code
COPY src /app/src

# Build the application
RUN ./gradlew build

# Use an official OpenJDK image to run the application
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built application from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the application runs on
EXPOSE 38081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]