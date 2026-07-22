FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S warehouse && adduser -S warehouse -G warehouse

COPY --from=build /workspace/target/*.jar app.jar

USER warehouse

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
