package br.com.unifametro.cearabank.notificacao.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do SpringDoc (Swagger/OpenAPI) para documentação.
 * Acessível em: http://localhost:8081/swagger-ui.html (se rodar na 8081)
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Ceará Bank Notificação API",
        version = "1.0",
        description = "Microserviço responsável por receber requisições de notificação e enviá-las para a fila de processamento (RabbitMQ).",
        contact = @Contact(
            name = "Time de Desenvolvimento - Unifametro",
            email = "devs@unifametro.com"
        ),
        license = @License(
            name = "Licença Propriedade Unifametro",
            url = "https://www.unifametro.com"
        )
    )
)
public class OpenApiConfig {
    // A anotação OpenAPIDefinition já configura a documentação
}