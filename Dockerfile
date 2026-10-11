FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/local-photo-sync-1.0.0-SNAPSHOT.jar app.jar
ENV VALKEY_HOST=localhost
ENV VALKEY_PORT=6379
ENTRYPOINT ["java", "-jar", "app.jar"]
