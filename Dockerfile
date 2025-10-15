# Use OpenJDK 21
FROM openjdk:21-jdk-slim

WORKDIR /app

# Install curl for debugging (optional)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy project files
COPY . .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build the Spring Boot JAR
RUN ./mvnw clean package -DskipTests

# Expose application port
#EXPOSE 8080

# Start the Spring Boot app directly
ENTRYPOINT ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
