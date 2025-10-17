package br.com.unifametro.cearabank.notificacao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de Segurança Mínima para permitir acesso público ao Swagger UI.
 * O Microserviço de Notificação não precisa de login para receber requisições internas (REST).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/notificacoes/**" // Permite o acesso ao endpoint da API também
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Desabilita CSRF, pois a API é stateless e não usará formulários.
            .csrf(csrf -> csrf.disable())
            
            // 2. Configuração de Autorização de Requisições
            .authorizeHttpRequests(auth -> auth
                // Permite acesso irrestrito aos caminhos do Swagger e ao nosso endpoint
                .requestMatchers(SWAGGER_PATHS).permitAll()
                // Garante que qualquer outra requisição também seja permitida, 
                // já que este serviço é cliente interno
                .anyRequest().permitAll()
            );

        // Se o seu projeto tiver Spring Security, o bloco acima deve ser suficiente
        // para desativar a tela de login para estes caminhos.
        
        return http.build();
    }
}
