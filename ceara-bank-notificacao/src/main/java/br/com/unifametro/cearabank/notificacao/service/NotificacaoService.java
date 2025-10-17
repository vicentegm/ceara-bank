package br.com.unifametro.cearabank.notificacao.service;

import br.com.unifametro.cearabank.notificacao.dto.NotificacaoRequestDTO;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por processar e simular o envio de notificações.
 * Removemos o RabbitMQ e simulamos o processamento assíncrono com Thread.sleep.
 */
@Service
public class NotificacaoService {

    /**
     * Recebe a requisição e simula o processamento e envio da notificação.
     * @param request DTO com os dados da transação.
     */
    public void processarENotificar(NotificacaoRequestDTO request) {
        
        // Simulação de processamento assíncrono (demora)
        try {
            System.out.println("\n*** LOG NOTIFICACAO: Iniciando Simulação de Envio (Atraso de 500ms) ***");
            Thread.sleep(500); // Simula o tempo que levaria para enviar um e-mail/SMS
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulação do processamento de envio de e-mail/SMS
        System.out.println("Transação ID: " + request.getIdTransacao());
        System.out.println("Status: " + request.getStatus());
        System.out.println("De: " + request.getContaOrigemId() + " | Para: " + request.getContaDestinoId());
        System.out.println("Valor: R$" + request.getValor());
        System.out.println("--- E-mail/SMS SIMULADO ENVIADO (Síncrono/Atrasado) ---\n");
    }
}
