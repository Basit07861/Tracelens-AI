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
EXPOSE 10000

# Tell Spring Boot to bind to 0.0.0.0 and listen on ${PORT:-10000}
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -Dserver.address=0.0.0.0 -jar app.jar"]