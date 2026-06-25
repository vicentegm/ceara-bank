FROM bellsoft/liberica-openjre-alpine:17
WORKDIR /app
COPY target/ceara-bank-seguranca-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]