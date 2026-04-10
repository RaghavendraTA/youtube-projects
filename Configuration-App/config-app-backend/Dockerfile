# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml
COPY pom.xml .

# Download dependencies
RUN mvn dependency:resolve

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /build/target/configuration-app-backend-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-XX:+UseVirtualThreads"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
