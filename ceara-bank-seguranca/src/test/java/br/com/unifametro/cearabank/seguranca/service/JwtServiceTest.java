package br.com.unifametro.cearabank.seguranca.service;

import br.com.unifametro.cearabank.seguranca.model.User;
import io.jsonwebtoken.ExpiredJwtException; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;

    private final String VALID_SECRET = "NTk1MzkwNGE2ZDc2Mzc2NDdhZWM2MjNiNmY1MzY4Mzc1OTZjNjMzNjU0NjI2MzYzNjE2NDY1Njk3NDcwNzc=";

    @BeforeEach
    void setUp() {
        // Valores padrão (1 hora de expiração)
        ReflectionTestUtils.setField(jwtService, "secretKey", VALID_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); 
        
        userDetails = new User("will.cearense", "senha123");
    }

    // --- Testes de Sucesso ---

    @Test
    void shouldGenerateAndExtractUsername() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(token.length() > 50);

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    void shouldValidateToken() {
        // Cria outro UserDetails para garantir que o teste está usando a comparação correta
        UserDetails validUser = new User("will.cearense", "outraSenha"); 
        String token = jwtService.generateToken(validUser);

        boolean isValid = jwtService.isTokenValid(token, validUser);
        assertTrue(isValid, "O token deve ser válido.");
    }
    
    // --- Testes de Falha (Validação) ---

    @Test
    void shouldFailForDifferentUsername() {
        // Token gerado para will.cearense
        String token = jwtService.generateToken(userDetails); 

        // Tenta validar com outro usuário (maxo.vehi)
        UserDetails wrongUser = new User("maxo.vehi", "senha123"); 
        
        boolean isValid = jwtService.isTokenValid(token, wrongUser);
        assertFalse(isValid, "A validação deve falhar para nome de usuário incorreto.");
    }
    
    @Test
    void shouldThrowExceptionForExpiredToken() {
        // Configura uma expiração muito curta (1 milissegundo)
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        
        // Gera o token
        String expiredToken = jwtService.generateToken(userDetails);
        
        // Testa se o método PÚBLICO que usa o parser (extractUsername)
        // lança a ExpiredJwtException.
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.extractUsername(expiredToken); 
        }, "A extração do Username deve falhar com ExpiredJwtException.");
    }
    
    @Test
    void shouldFailForInvalidSignature() {
        // Gera um token com a chave correta
        String validToken = jwtService.generateToken(userDetails);
        
        // Tenta usar um JwtService com uma chave *diferente* (simula alteração no token ou chave diferente)
        ReflectionTestUtils.setField(jwtService, "secretKey", "MTEyMzUxMjM1MjM2MTEyMzY1NTEyMzY1MTEyMzY1MTEyMzY1MTEyMzY1NTEyMzY1MTEyMzY1MTQ0NTU2NTY=");
        
        // A extração de claims deve lançar uma exceção de assinatura inválida (SignatureException)
        assertThrows(RuntimeException.class, () -> { // RuntimeException é genérica, mas SignatureException é a esperada
            jwtService.extractUsername(validToken); 
        }, "A extração de claims deve falhar para token com assinatura inválida.");
    }
}