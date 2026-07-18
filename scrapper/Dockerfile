# Этап 1: сборка с Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: минимальный образ для запуска
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/myapp.jar app.jar
CMD ["java", "-jar", "app.jar"]
