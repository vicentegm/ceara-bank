# Stage 1: Buildar o projeto
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Rodar a aplicação
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copia o jar gerado no build (ajusta o nome se o teu for diferente)
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]