package br.com.unifametro.cearabank.contas.config;


import br.com.unifametro.cearabank.contas.filter.TokenValidationFilter; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    // Injete o novo filtro (que criaremos no Passo 2)
    private final TokenValidationFilter tokenValidationFilter;
    
    public SecurityConfig(TokenValidationFilter tokenValidationFilter) {
        this.tokenValidationFilter = tokenValidationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(authorize -> authorize
                // -------------------------------------------------------------
                // ESTA SEÇÃO DEVE SER A PRIMEIRA A SER AVALIADA!
                // Permite acesso livre ao Swagger (documentação)
                .requestMatchers(
                    "/swagger-ui.html", 
                    "/v3/api-docs/**", 
                    "/swagger-ui/**",
                    "/webjars/**" // Adicionando webjars por segurança
                ).permitAll()
                // -------------------------------------------------------------
                
                // EXIGE AUTENTICAÇÃO para o restante
                .anyRequest().authenticated()
            )
            
            // Adiciona nosso filtro customizado ANTES do processamento padrão do Spring Security
            .addFilterBefore(tokenValidationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}