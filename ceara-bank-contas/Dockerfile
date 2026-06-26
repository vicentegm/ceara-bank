# Stage 1: Buildar o projeto
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Rodar a aplicação
FROM eclipse-temurin:17-jre
WORKDIR /app
# Agora sim, ele busca lá do stage 'build' que definimos em cima
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]