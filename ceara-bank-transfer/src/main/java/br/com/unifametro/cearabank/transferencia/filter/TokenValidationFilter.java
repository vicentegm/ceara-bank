package br.com.unifametro.cearabank.transferencia.filter;

import br.com.unifametro.cearabank.transferencia.config.AuthContext; 
import br.com.unifametro.cearabank.transferencia.dto.seguranca.TokenValidationResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenValidationFilter extends OncePerRequestFilter {

    @Value("${microservicos.security.url-validacao-token}")
    private String securityServiceValidationUrl; 

    private final RestTemplate restTemplate = new RestTemplate();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // Constante para o nome do atributo onde o username será armazenado
    public static final String USERNAME_ATTRIBUTE = "authenticatedUsername";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Padrões de caminhos que NÃO devem passar pela validação de Token
        String[] excludedPaths = {
            "/swagger-ui.html", 
            "/swagger-ui/**", 
            "/v3/api-docs/**",
            "/actuator/**" 
        };
        
        String requestUri = request.getRequestURI();

        for (String path : excludedPaths) {
            if (pathMatcher.match(path, requestUri)) {
                return true; 
            }
        }
        return false; 
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader; // Mantém o token completo (Bearer <jwt>)
        }

        try {
            if (token != null) {
                // *** 1. AÇÃO CRÍTICA: REPASSA O TOKEN PARA O CONTEXTO DA THREAD ***
                AuthContext.setToken(token); 
                
                HttpHeaders headers = new HttpHeaders();
                // Passamos o token completo para o microsserviço de segurança para validação
                headers.set(HttpHeaders.AUTHORIZATION, token);
                HttpEntity<String> entity = new HttpEntity<>(headers); 
                
                try {
                    // 2. Chama o microserviço de Segurança para validar
                    ResponseEntity<TokenValidationResponse> validationResponse = restTemplate.exchange(
                        securityServiceValidationUrl, 
                        HttpMethod.GET, 
                        entity, 
                        TokenValidationResponse.class
                    );
                    
                    TokenValidationResponse body = validationResponse.getBody();
                    
                    // 3. Se válido, injeta o username na requisição e continua a cadeia
                    if (validationResponse.getStatusCode() == HttpStatus.OK && 
                        body != null && body.isValid() && 
                        body.getUsername() != null) {
                        
                        request.setAttribute(USERNAME_ATTRIBUTE, body.getUsername());
                        
                        filterChain.doFilter(request, response);
                        return;

                    } else {
                        sendUnauthorized(response, "Token JWT inválido, mas serviço de segurança retornou OK.");
                        return;
                    }

                } catch (HttpClientErrorException e) {
                    // Erro 401 ou 403 do serviço de segurança
                    sendUnauthorized(response, "Token JWT inválido ou expirado.");
                    return;
                } catch (Exception e) {
                     // Erro de I/O ou comunicação
                     sendUnauthorized(response, "Erro de rede/comunicação durante a validação do token.");
                     return;
                }
            }

            // 4. Se não houver token
            sendUnauthorized(response, "Acesso negado. Token Bearer não encontrado no cabeçalho Authorization.");
            
        } finally {
             // *** 5. AÇÃO CRÍTICA: SEMPRE LIMPA O CONTEXTO DA THREAD NO FINAL ***
             AuthContext.clear(); 
        }
    }
    
    // Método auxiliar para escrever a resposta de falha 401
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
    }
}
