package br.com.unifametro.cearabank.contas.controller;

import br.com.unifametro.cearabank.contas.dto.MovimentacaoRequestDTO;
import br.com.unifametro.cearabank.contas.dto.SaldoResponseDTO;
import br.com.unifametro.cearabank.contas.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/contas")
@Tag(name = "Contas", description = "Endpoints para consulta de saldo e movimentação atômica de contas.")
public class ContaController {

    private final ContaService service;

    public ContaController(ContaService service) {
        this.service = service;
    }

    /**
     * Consulta o saldo de uma conta pelo username. Usado pelo microserviço de Transferência.
     */
    @Operation(summary = "Consulta Saldo por Username",
               description = "Endpoint interno que retorna o saldo e titular de uma conta.")
    @GetMapping("/saldo/{username}")
    public ResponseEntity<SaldoResponseDTO> consultarSaldo(@PathVariable("username") String username) {
        try {
            SaldoResponseDTO response = service.consultarSaldo(username);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Conta não encontrada
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Realiza o débito na conta de origem após validação de saldo. Operação atômica.
     */
    @Operation(summary = "Realiza Débito Atômico",
               description = "Endpoint interno para debitar o valor de uma conta. Retorna 204 se sucesso.")
    @PostMapping("/debitar")
    public ResponseEntity<Void> processarDebito(@RequestBody MovimentacaoRequestDTO request) {
        try {
            service.processarDebito(request);
            // Retorna 204 No Content para indicar sucesso sem corpo de resposta
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // Erro de negócio (Conta não existe ou Saldo Insuficiente)
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Erro de sistema
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Endpoint utilitário para adicionar uma conta ao banco de dados (apenas para testes iniciais).
     */
    @Operation(summary = "Adiciona Conta para Teste (Utilitário)",
               description = "Cria uma conta com saldo inicial para fins de teste e desenvolvimento.")
    @PostMapping("/criarContaTeste")
    public ResponseEntity<String> criarContaTeste(@RequestParam String id, @RequestParam BigDecimal saldoInicial) {
        service.criarContaTeste(id, saldoInicial);
        return ResponseEntity.status(HttpStatus.CREATED).body("Conta " + id + " criada com saldo: " + saldoInicial);
    }
}