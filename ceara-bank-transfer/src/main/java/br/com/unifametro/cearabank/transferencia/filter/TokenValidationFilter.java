package br.com.unifametro.cearabank.transferencia.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity; // NOVO: Para o corpo da requisição
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod; // NOVO: Para o método GET com cabeçalho
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenValidationFilter extends OncePerRequestFilter {

    // INJEÇÃO: Pega a URL de validação do application.yml
    @Value("${microservicos.security.url-validacao-token}")
    private String securityServiceValidationUrl; 

    private final RestTemplate restTemplate = new RestTemplate();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // ----------------------------------------------------------------------
    // MÉTODO PARA IGNORAR O FILTRO EM CAMINHOS ESPECÍFICOS (Swagger)
    // ----------------------------------------------------------------------
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
            // Se o URI da requisição corresponder a algum caminho excluído, IGNORA o filtro.
            if (pathMatcher.match(path, requestUri)) {
                return true; 
            }
        }
        return false; 
    }
    // ----------------------------------------------------------------------

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 1. Verifica se o cabeçalho Authorization está presente e é do tipo Bearer
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            
            // --- CÓDIGO NOVO PARA ENVIAR CABEÇALHO PARA O REST TEMPLATE ---
            
            // Cria o cabeçalho e adiciona o token completo (Bearer <token>)
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers); 
            
            // --- FIM DO CÓDIGO NOVO ---

            try {
                // 2. Chama o microserviço de Segurança (8081) para validar o token
                // Usa exchange() para enviar o cabeçalho Authorization com o Bearer Token
                restTemplate.exchange(
                    securityServiceValidationUrl, 
                    HttpMethod.GET, 
                    entity, 
                    Void.class // Não esperamos corpo na resposta (apenas status 200)
                );
                
                // 3. Se a chamada retornar 200 (OK), o token é válido.
                filterChain.doFilter(request, response);
                return;

            } catch (HttpClientErrorException e) {
                // 4. Se retornar 401 ou 403 do microserviço de segurança
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write("Token inválido ou expirado.");
                    return;
                }
                // Propaga outros erros HTTP
                throw e; 
            }
        }

        // Se não houver token ou não for Bearer, o acesso é negado (401 Unauthorized)
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write("Acesso negado. Token Bearer não encontrado.");
    }
}