# --- Build Stage ---
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY . .
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests clean package

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
