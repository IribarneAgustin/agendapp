FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /app
COPY . .
RUN mvn -DskipTests clean package

FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY --from=build /app/target/api-0.0.1-SNAPSHOT.jar api.jar
EXPOSE 8080


ENTRYPOINT ["java", "-jar", "api.jar"]