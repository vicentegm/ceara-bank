package br.com.unifametro.cearabank.seguranca.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
    @Schema(description = "Token JWT de acesso", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IldpbGwiLCJpYXQiOjE1MTYyMzkwMjJ9")
    String token,
    
    @Schema(description = "Tipo do token (sempre 'Bearer')", example = "Bearer")
    String type
) {
    // Este DTO retorna o token JWT após o login
}