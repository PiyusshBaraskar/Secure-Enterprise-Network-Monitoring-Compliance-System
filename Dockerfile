# ===================================================================
# DOCKERFILE - SECURE ENTERPRISE NETWORK MONITOR (JAVA 8 COMPATIBLE)
# ===================================================================

# Stage 1: Build the Maven application
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app
COPY pom.xml .
# Cache Maven dependencies
RUN mvn dependency:go-offline -B
COPY src ./src
# Build the JAR
RUN mvn clean package -DskipTests

# Stage 2: Create runtime image
FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /app/target/monitor-1.0.0-SNAPSHOT.jar app.jar

# Create folder for file reports
RUN mkdir -p /app/reports

# Expose port
EXPOSE 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
