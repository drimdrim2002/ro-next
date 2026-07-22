FROM maven:3.9.14-eclipse-temurin-25 AS build

WORKDIR /workspace
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM amazoncorretto:25-al2023

WORKDIR /app
COPY --from=build /workspace/target/ro-next-0.1.0-SNAPSHOT-app.jar /app/app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
