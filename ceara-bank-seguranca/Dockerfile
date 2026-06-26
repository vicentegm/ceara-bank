# Usa a imagem base do eclipse-temurin que é bem mais leve e lida melhor com cross-arch
FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine AS build

# Instala o Maven manualmente, já que o eclipse-temurin não vem com ele
RUN apk add --no-cache maven

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# O resto do teu Dockerfile continua igual...
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]