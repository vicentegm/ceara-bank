package br.com.unifametro.cearabank.notificacao.controller;

import br.com.unifametro.cearabank.notificacao.dto.NotificacaoRequestDTO;
import br.com.unifametro.cearabank.notificacao.service.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation; 
import io.swagger.v3.oas.annotations.tags.Tag; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller que expõe o endpoint para o serviço de Transferências chamar
 * e ENVIAR a mensagem para a FILA.
 */
@RestController
@RequestMapping("/notificacoes")
@Tag(name = "Notificações", description = "Endpoints para envio e controle de notificações assíncronas.") // Anotação do Swagger
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @Autowired
    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

   /**
     * Endpoint para receber a requisição de notificação de transação e processá-la.
     * Mesmo que a implementação seja síncrona/simulada, mantemos o retorno 202 Accepted.
     */
    @Operation(summary = "Envia uma notificação transacional para processamento assíncrono (Simulado).",
               description = "O Transferencia Service chama este endpoint para notificar uma transação.") 
    @PostMapping("/enviar-para-fila")
    public ResponseEntity<Void> notificarTransacao(@RequestBody NotificacaoRequestDTO request) {
        
        System.out.println("LOG CONTROLLER: Requisição de notificação recebida via REST. Processando simulação...");
        
        // O service apenas processa e simula o envio, não retorna String
        notificacaoService.processarENotificar(request);
        
        // Retorna 202 Accepted (sem corpo), mantendo a semântica de processamento assíncrono
        return ResponseEntity.accepted().build(); 
    }
}