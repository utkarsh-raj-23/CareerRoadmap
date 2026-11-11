# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle

COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew

RUN ./gradlew bootJar -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/Billing-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
