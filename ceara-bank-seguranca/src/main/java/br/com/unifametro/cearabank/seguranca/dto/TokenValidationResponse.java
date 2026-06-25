package br.com.unifametro.cearabank.seguranca.dto;

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
    
    // Construtor manual para SUCESSO. Ele chama o @AllArgsConstructor (super()) de forma implícita.
    // Ele garante que o campo 'message' seja padronizado no sucesso.
    public TokenValidationResponse(boolean valid, String username) {
        this(valid, username, "Token validado com sucesso."); // Chama o @AllArgsConstructor
    }

    /*
     * REMOVIDO: O construtor manual para falha (boolean, String message)
     * causava o conflito com o construtor acima, pois tinha a mesma assinatura
     * se o 'username' fosse nulo (boolean, String).
     *
     * Agora, usaremos o construtor de sucesso para falhas, passando 'null' para username,
     * ou usaremos o @AllArgsConstructor diretamente para falhas, o que é mais limpo.
     *
     * Se você usou o construtor manual de falha no controller, mude ele para usar o construtor
     * de 3 argumentos (@AllArgsConstructor) ou o construtor de 2 argumentos, assim:
     */
}