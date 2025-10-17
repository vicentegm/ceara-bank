package br.com.unifametro.cearabank.contas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

/**
 * Configuração do RestTemplate para incluir automaticamente o Bearer Token
 * em todas as requisições de saída (Service-to-Service).
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Define o Bean do RestTemplate com o Interceptor.
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // Adiciona o interceptor que injeta o token JWT.
        restTemplate.setInterceptors(Collections.singletonList(new TokenInterceptor()));
        return restTemplate;
    }

    /**
     * Interceptor que verifica o AuthContext e adiciona o token JWT ao cabeçalho.
     */
    private static class TokenInterceptor implements ClientHttpRequestInterceptor {
        
        @Override
        public ClientHttpResponse intercept(
                HttpRequest request, 
                byte[] body, 
                ClientHttpRequestExecution execution) throws IOException {

            String token = AuthContext.getToken();
            
            // Se houver um token na Thread, adicione-o ao cabeçalho Authorization
            if (token != null) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, token);
            }
            
            // Procede com a execução da requisição
            return execution.execute(request, body);
        }
    }
}
