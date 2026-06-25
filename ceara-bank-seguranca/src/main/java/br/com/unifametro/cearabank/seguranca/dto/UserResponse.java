package br.com.unifametro.cearabank.seguranca.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Supondo que você está usando um Record para DTOs
public record UserResponse(
    @Schema(description = "Identificador único do usuário", example = "101")
    Long id,
    
    @Schema(description = "Nome de usuário/login", example = "will_ceara")
    String username
) {
    
}