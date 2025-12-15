# Stage 1: Build the application with Maven & Java 21
FROM maven:3.9.3-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
# Copy source code
COPY src ./src

# Build without running tests
RUN mvn clean package -DskipTests

# Stage 2: Run the application with Java 21
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy the built jar from stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose backend port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
