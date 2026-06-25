package br.com.unifametro.cearabank.seguranca.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Imports do Swagger/OpenAPI (Onde estava o erro!)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários (Protegido)", description = "Endpoints que requerem autenticação JWT.")
public class UserController {

    @GetMapping("/perfil")
    @Operation(
        summary = "Obter Perfil do Usuário Autenticado",
        description = "Retorna os detalhes do usuário atual, exigindo um Bearer Token.",
        security = @SecurityRequirement(name = "bearerAuth") // Anotação chave!
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sucesso. Dados do usuário retornados."),
        @ApiResponse(responseCode = "401", description = "Não Autorizado: Token JWT ausente ou inválido."),
        @ApiResponse(responseCode = "403", description = "Acesso Negado: Usuário sem permissão.")
    })
    public ResponseEntity<String> getPerfil() {
        return ResponseEntity.ok("Perfil do usuário!");
    }
}