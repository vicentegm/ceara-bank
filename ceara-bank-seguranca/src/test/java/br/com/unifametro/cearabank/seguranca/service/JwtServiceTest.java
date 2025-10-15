package br.com.unifametro.cearabank.seguranca.service;

import br.com.unifametro.cearabank.seguranca.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Usa ReflectionTestUtils para injetar valores @Value (chave secreta e expiração)
        // Isso simula o comportamento do Spring sem carregar o contexto inteiro.
        ReflectionTestUtils.setField(jwtService, "secretKey", "NTk1MzkwNGE2ZDc2Mzc2NDdhZWM2MjNiNmY1MzY4Mzc1OTZjNjMzNjU0NjI2MzYzNjE2NDY1Njk3NDcwNzc=");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hora
        
        // Cria um UserDetails de simulação (Mock)
        userDetails = new User("will.cearense", "senha123");
    }

    @Test
    void shouldGenerateAndExtractUsername() {
        // 1. Geração do Token
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(token.length() > 50);

        // 2. Extração do Username
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    void shouldValidateToken() {
        // Geração
        String token = jwtService.generateToken(userDetails);

        // Validação
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid, "O token deve ser válido.");
    }
    
    // Teste para demonstrar a falha (com token expirado)
    @Test
    void shouldFailForExpiredToken() throws InterruptedException {
        // Configura uma expiração muito curta (1 milissegundo)
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        
        // Geração do token
        String expiredToken = jwtService.generateToken(userDetails);
        
        // Espera para garantir que a expiração passou
        Thread.sleep(50); 
        
        // Validação (deve falhar)
        boolean isValid = jwtService.isTokenValid(expiredToken, userDetails);
        assertFalse(isValid, "O token deve ser considerado expirado.");
    }
}