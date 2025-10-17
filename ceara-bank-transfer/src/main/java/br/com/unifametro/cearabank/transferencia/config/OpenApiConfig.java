package br.com.unifametro.cearabank.transferencia.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CearaBank - Microserviço de Transferências",
                version = "v1.0.0",
                description = "API para processamento, consulta e agendamento de transferências (PIX, TED, DOC) do CearaBank.",
                contact = @Contact(
                        name = "Vicente Magalhaes - Unifametro",
                        email = "vicentemagalhaesjr@cearabank.com.br"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        // Garante que o Bearer Token seja o método de segurança padrão
        security = @SecurityRequirement(name = "BearerAuth")
)
@SecurityScheme(
        name = "BearerAuth", // Nome do esquema (usado no SecurityRequirement acima)
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Token de Acesso fornecido pelo serviço de Autenticação (Login)."
)
public class OpenApiConfig {
    // Esta classe não precisa de métodos, as anotações configuram o Swagger/OpenAPI.
}