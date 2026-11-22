# ---- Stage 1: Build with Maven ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---- Stage 2: Lightweight runtime ----
FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.source=https://github.com/abdelilahraees/hrayfi-backend
LABEL org.opencontainers.image.description="Hrayfi backend - Spring Boot API for the artisan/client marketplace"
LABEL org.opencontainers.image.licenses=MIT

WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]