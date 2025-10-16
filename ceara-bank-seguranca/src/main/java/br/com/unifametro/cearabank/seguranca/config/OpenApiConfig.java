package br.com.unifametro.cearabank.seguranca.config;

import org.springframework.context.annotation.Configuration;

// Imports do Swagger/OpenAPI
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;


@Configuration
@OpenAPIDefinition(
    // Metadados da API
    info = @Info(
        title = "API Ceará Bank - Microsserviço de Segurança (JWT)", // Título
        version = "1.0.0", // Versão
        description = "API responsável pela autenticação (Login) e gestão inicial de usuários (Registro) para os demais microsserviços do Ceará Bank.", 
        contact = @Contact(
            name = "Unifametro Engenharia de Sofware", // Seu nome e curso
            email = "vicentemagalhaesjunior@cearabank.com" // Seu email
        )
    ),
    // Define os servidores onde a API pode ser acessada
    servers = {
        @Server(url = "http://172.17.0.3:8082", description = "Servidor Local (Desenvolvimento)"),
        @Server(url = "https://api.cearabank.com/security", description = "Servidor de Produção (Exemplo)")
    }
)
@SecurityScheme( // Configura o esquema de segurança JWT (Bearer Token)
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Insira seu Token JWT após o login. O formato deve ser: Bearer [token]"
)
public class OpenApiConfig {
    // Esta classe é apenas para carregar as configurações acima no Spring Context
}