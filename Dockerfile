# Stage 1: Build the Java application using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the JAR file (skipping tests to speed up deploy)
RUN mvn clean package -DskipTests

# Stage 2: Run application with Lightweight Java Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Command to start the backend application
ENTRYPOINT ["java", "-jar", "app.jar"]