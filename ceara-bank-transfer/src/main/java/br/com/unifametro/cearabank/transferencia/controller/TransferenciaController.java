package br.com.unifametro.cearabank.transferencia.controller;

import br.com.unifametro.cearabank.transferencia.dto.TransferenciaRequestDTO;
import br.com.unifametro.cearabank.transferencia.dto.TransferenciaResponseDTO;
import br.com.unifametro.cearabank.transferencia.enums.StatusTransferencia;
import br.com.unifametro.cearabank.transferencia.filter.TokenValidationFilter;

import br.com.unifametro.cearabank.transferencia.service.TransferenciaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest; 
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1/transferencias")
@Tag(name = "Transferências", description = "Endpoints para gerenciamento de transações financeiras (PIX, TED, DOC)")
public class TransferenciaController {

    @Autowired
    private TransferenciaService service;

    // 1. Inicia uma nova transferência (ENDPOINT PRINCIPAL)
    // Agora unificado para receber o HttpServletRequest e a lógica do Token.
    @Operation(summary = "Inicia uma nova transferência (PIX/TED/DOC)", 
               description = "Processa a validação e o registro imediato de uma transação. Requer Token Bearer.")
    @PostMapping
    public ResponseEntity<?> iniciarTransferencia(
            HttpServletRequest request, // Injetamos o request para pegar o username
            @Valid @RequestBody TransferenciaRequestDTO dto) {
        
        // 1. OBTÉM O REMETENTE AUTORIZADO DO FILTRO (Exclusivamente do Token)
        String remetenteUsername = (String) request.getAttribute(TokenValidationFilter.USERNAME_ATTRIBUTE);

        if (remetenteUsername == null) {
            // Se o filtro não injetou, algo falhou no filtro ou o acesso foi direto.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message", "Usuário remetente não identificado (falha de segurança na sessão).")
            );
        }

        try {
            // 2. Chama o Serviço para processar a transferência
            // Passamos o remetenteUsername (ID da conta/usuário) e o DTO
            TransferenciaResponseDTO response = service.processarTransferencia(remetenteUsername, dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // 3. Captura erros de validação (Saldo insuficiente, conta inválida, etc.)
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            // 4. Captura erros gerais (comunicação, DB, etc.)
            return ResponseEntity.internalServerError().body(
                Map.of("message", "Erro interno ao processar a transferência: " + e.getMessage())
            );
        }
    }

    // 2. Consulta o status de uma transferência específica
    @Operation(summary = "Consulta o status de uma transferência por ID", 
               description = "Retorna os detalhes de uma transação específica pelo ID único gerado.")
    @GetMapping("/{id}")
    public ResponseEntity<TransferenciaResponseDTO> consultarStatus(
            @Parameter(description = "ID da transação", example = "a1b2c3d4e5f6") @PathVariable String id) {
        
        Optional<TransferenciaResponseDTO> response = service.consultarStatus(id);
        
        return response.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // [Mantenha os outros métodos (listarExtratoConta, listarPendentes, estornarTransferencia, agendarTransferencia, cancelarAgendamento, getResumoDiario) inalterados]
    
    // ... Aqui iriam os métodos 3 a 8 que você já tinha no controller...
    
    // 3. Lista o extrato de transferências por conta
    @Operation(summary = "Lista o extrato de transferências de uma conta", 
               description = "Retorna todas as transações de origem ou destino de uma conta específica, ordenadas pela data.")
    @GetMapping("/extrato/{conta}")
    public ResponseEntity<List<TransferenciaResponseDTO>> listarExtratoConta(
            @Parameter(description = "Número da conta", example = "12345-X") @PathVariable String conta) {
        
        List<TransferenciaResponseDTO> extrato = service.listarExtratoConta(conta);
        return ResponseEntity.ok(extrato);
    }
    
    // 4. Lista todas as transferências pendentes (para um painel admin)
    @Operation(summary = "Lista transações pendentes de processamento", 
               description = "Retorna uma lista de todas as transações com status PENDENTE.")
    @GetMapping("/pendentes")
    public ResponseEntity<List<TransferenciaResponseDTO>> listarPendentes() {
        
        List<TransferenciaResponseDTO> pendentes = service.listarPorStatus(StatusTransferencia.PENDENTE);
        return ResponseEntity.ok(pendentes);
    }
    
    // 5. Estorna uma transferência (Geralmente via Admin/Suporte)
    @Operation(summary = "Estorna uma transação concluída", 
               description = "Altera o status da transação para ESTORNADA e dispara a lógica de reversão de fundos.")
    @PutMapping("/estornar/{id}")
    public ResponseEntity<TransferenciaResponseDTO> estornarTransferencia(
            @Parameter(description = "ID da transação a ser estornada") @PathVariable String id) {
        
        Optional<TransferenciaResponseDTO> response = service.estornarTransferencia(id);
        
        return response.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // 6. Agenda uma transferência (futura)
    @Operation(summary = "Agenda uma transferência para o futuro", 
               description = "Registra uma transação com status AGENDADA para ser processada por um scheduler.")
    @PostMapping("/agendar")
    public ResponseEntity<TransferenciaResponseDTO> agendarTransferencia(
            @Valid @RequestBody TransferenciaRequestDTO dto,
            @Parameter(description = "Data e Hora do agendamento (formato ISO)", example = "2025-12-31T10:00:00") 
            @RequestParam String dataHoraAgendamento) {
        
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraAgendamento);
        
        TransferenciaResponseDTO response = service.agendarTransferencia(dto, dataHora);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response); // 202 Accepted
    }
    
    // 7. Cancela uma transferência agendada
    @Operation(summary = "Cancela uma transferência previamente agendada", 
               description = "Remove a transação do banco de dados se o status for AGENDADA.")
    @DeleteMapping("/agendar/{id}")
    public ResponseEntity<Void> cancelarAgendamento(
            @Parameter(description = "ID da transação agendada") @PathVariable String id) {
        
        boolean cancelado = service.cancelarAgendamento(id);
        
        return cancelado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    // 8. Consulta o resumo diário de transferências (para relatórios)
    @Operation(summary = "Calcula o volume total de transferências em um dia", 
               description = "Retorna a soma dos valores de todas as transações concluídas (SUCESSO) na data especificada.")
    @GetMapping("/resumo/diario")
    public ResponseEntity<String> getResumoDiario(
            @Parameter(description = "Data para consulta (formato YYYY-MM-DD)", example = "2025-10-16") @RequestParam String data) {
        
        // O Service retorna o valor, aqui apenas formatamos a resposta
        String valorFormatado = "R$ " + service.calcularTotalTransferidoNoDia(data).toString();
        
        return ResponseEntity.ok("Resumo diário para " + data + ": " + valorFormatado);
    }
}