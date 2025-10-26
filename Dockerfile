# ---- Stage 1: Build the app with Maven (JDK 21) ----
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies (for caching)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the Spring Boot jar (skip tests for faster deploy)
RUN mvn clean package -DskipTests

# ---- Stage 2: Run the app with a slim JRE ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/helmaaibackend-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (Render will route traffic here)
EXPOSE 8080

# Force Spring Boot to use prod profile inside the container
ENV SPRING_PROFILES_ACTIVE=prod

# MONGODB_URI will be injected by Render as an environment variable
ENTRYPOINT ["java", "-jar", "app.jar"]
