# Stage 1: Build using Maven 3.9.3 with JDK 21
FROM maven:3.9.3-jdk-21 AS build
WORKDIR /app

# Copy pom.xml first for caching
COPY pom.xml .
# Copy source code
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Stage 2: Run using JDK 21
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy built JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
