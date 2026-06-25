package br.com.unifametro.cearabank.contas.filter;

import br.com.unifametro.cearabank.contas.config.AuthContext; 
import br.com.unifametro.cearabank.contas.dto.seguranca.TokenValidationResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class TokenValidationFilter extends OncePerRequestFilter {

    @Value("${microservicos.security.url-validacao-token}")
    private String securityServiceValidationUrl; 

    private final RestTemplate restTemplate = new RestTemplate();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    public static final String USERNAME_ATTRIBUTE = "authenticatedUsername";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                AuthContext.setToken(authHeader);
                
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                HttpEntity<String> entity = new HttpEntity<>(headers); 
                
                ResponseEntity<TokenValidationResponse> validationResponse = restTemplate.exchange(
                    securityServiceValidationUrl, HttpMethod.GET, entity, TokenValidationResponse.class
                );
                
                TokenValidationResponse body = validationResponse.getBody();
                
                if (validationResponse.getStatusCode() == HttpStatus.OK && body != null && body.isValid()) {
                    logger.info("Usuário autenticado com sucesso: " + body.getUsername());

                    
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        body.getUsername(), null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    
                    request.setAttribute(USERNAME_ATTRIBUTE, body.getUsername());
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                logger.error("Erro na validação do token para a URI: " + request.getRequestURI(), e);
                sendUnauthorized(response, "Falha na validação do token: " + e.getMessage());
                return;
            } finally {
                AuthContext.clear();
            }
        }
        sendUnauthorized(response, "Token ausente ou inválido.");
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\": 401, \"message\": \"" + message + "\"}");
    }
}