package br.com.unifametro.cearabank.transferencia.dto.seguranca;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenValidationResponse {
    
    private boolean valid;
    private String username;
    private String message;
    
    // Construtor manual para SUCESSO (opcional, mas bom manter a compatibilidade)
    public TokenValidationResponse(boolean valid, String username) {
        this(valid, username, "Token validado com sucesso.");
    }
}