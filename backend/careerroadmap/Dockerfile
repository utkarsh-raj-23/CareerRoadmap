
# Use Java 21
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy Spring Boot JAR
COPY target/*.jar app.jar

# Expose application port
EXPOSE 10000

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
