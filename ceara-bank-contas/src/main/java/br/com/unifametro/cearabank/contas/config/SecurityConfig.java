package br.com.unifametro.cearabank.contas.config;

import br.com.unifametro.cearabank.contas.filter.TokenValidationFilter; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final TokenValidationFilter tokenValidationFilter;
    
    public SecurityConfig(TokenValidationFilter tokenValidationFilter) {
        this.tokenValidationFilter = tokenValidationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(authorize -> authorize
                // Libera Actuator, Robots e Swagger pra o sistema respirar
                .requestMatchers(
                    "/actuator/**", 
                    "/robots.txt", 
                    "/robots*.txt",
                    "/swagger-ui.html", 
                    "/v3/api-docs/**", 
                    "/swagger-ui/**",
                    "/webjars/**"
                ).permitAll()
                
                // O resto é tudo trancado
                .anyRequest().authenticated()
            )
            
            // Filtro customizado na frente da fila
            .addFilterBefore(tokenValidationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}