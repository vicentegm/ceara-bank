package br.com.unifametro.cearabank.transferencia.filter;

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
import java.util.Objects;

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
            "/v3/api-docs/**"
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

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            
            HttpHeaders headers = new HttpHeaders();
            // Passamos o token completo (Bearer <token>) para o microsserviço de segurança
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers); 
            
            try {
                // 1. Chama o microserviço de Segurança e espera o DTO de Resposta
                ResponseEntity<TokenValidationResponse> validationResponse = restTemplate.exchange(
                    securityServiceValidationUrl, 
                    HttpMethod.GET, 
                    entity, 
                    TokenValidationResponse.class // <--- AGORA ESPERAMOS O DTO!
                );
                
                TokenValidationResponse body = validationResponse.getBody();
                
                // 2. Se a resposta for 200 e o corpo for válido:
                if (validationResponse.getStatusCode() == HttpStatus.OK && 
                    body != null && body.isValid() && 
                    body.getUsername() != null) {
                    
                    // 3. O token é válido. Injeta o username no atributo da requisição.
                    request.setAttribute(USERNAME_ATTRIBUTE, body.getUsername());
                    
                    filterChain.doFilter(request, response);
                    return;

                } else {
                    // Resposta 200, mas corpo inválido (muito improvável)
                    sendUnauthorized(response, "Validação de Token falhou, mas status 200 retornado.");
                    return;
                }

            } catch (HttpClientErrorException e) {
                // 4. Se retornar 401 ou 403 do microsserviço de segurança
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    sendUnauthorized(response, "Token inválido ou expirado.");
                    return;
                }
                // Propaga outros erros HTTP
                throw new ServletException("Erro de comunicação com o serviço de segurança: " + e.getMessage(), e); 
                
            } catch (Exception e) {
                 // Erro de I/O, falha de rede ou desserialização do JSON
                 sendUnauthorized(response, "Erro de rede/comunicação durante a validação: " + e.getMessage());
                 return;
            }
        }

        // 5. Se não houver token ou não for Bearer
        sendUnauthorized(response, "Acesso negado. Token Bearer não encontrado.");
    }
    
    // Método auxiliar para escrever a resposta de falha 401
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
    }
}