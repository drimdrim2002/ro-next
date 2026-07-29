FROM maven:3.9.14-eclipse-temurin-25 AS build

WORKDIR /workspace
COPY . .
RUN ./mvnw -B -ntp -DskipTests -pl legacy/gcp-placeholder -am dependency:go-offline
RUN ./mvnw -B -ntp -DskipTests -pl legacy/gcp-placeholder -am package

FROM amazoncorretto:25-al2023

WORKDIR /app
COPY --from=build /workspace/legacy/gcp-placeholder/target/legacy-gcp-placeholder-0.1.0-SNAPSHOT-app.jar /app/app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
