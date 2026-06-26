# Estágio de build (Usa o Maven pra compilar e gerar o jar)
FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine AS build

# Instala o Maven, o bicho precisa disso pra rodar o 'package'
RUN apk add --no-cache maven

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Estágio de execução (Bem mais leve pra não travar o Azure)
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine

# Define o diretório de trabalho padrão
WORKDIR /app

# Copia só o jar gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Define a porta que o container expõe
EXPOSE 8081

# Força o Java a subir na porta 8081 e evita travamento no startup
ENTRYPOINT ["java", "-Dserver.port=8081", "-jar", "app.jar"]