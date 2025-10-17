package br.com.unifametro.cearabank.transferencia.service;

import br.com.unifametro.cearabank.transferencia.model.Transferencia;
import br.com.unifametro.cearabank.transferencia.dto.NotificacaoRequestDTO; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Serviço responsável pela integração com o microserviço de Notificação via HTTP (REST).
 */
@Service
public class NotificacaoIntegratorService {

    private final RestTemplate restTemplate;

    // URL base do serviço Notificacao, lido do application.properties
    @Value("${service.notificacao.url:http://localhost:8081}") 
    private String notificacaoServiceUrl; 

    public NotificacaoIntegratorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Converte o objeto Transferencia em um DTO de Notificação e envia via REST.
     * @param transferencia O objeto Transferencia concluído.
     */
    public void notificarAssincronamente(Transferencia transferencia) {
        // Mapeamento da Entidade Transferencia para o DTO de Requisição de Notificação
        NotificacaoRequestDTO notificacaoDTO = new NotificacaoRequestDTO(
            transferencia.getId(),
            transferencia.getContaOrigemId(),
            transferencia.getContaDestinoId(),
            transferencia.getValor(),
            transferencia.getDataTransacao(),
            transferencia.getStatus()
        );

        String url = notificacaoServiceUrl + "/notificacoes/enviar-para-fila";
        
        try {
            // A chamada é não-bloqueante do ponto de vista do Transferencia Service, 
            // pois o Notificacao Service apenas enfileira e retorna 202 Accepted.
            restTemplate.postForEntity(url, notificacaoDTO, String.class);
            System.out.println("LOG INTEGRACAO: Requisição de notificação enviada com sucesso para: " + url);
        } catch (Exception e) {
            // Logamos a falha, mas NÃO falhamos a transação principal (é um erro secundário)
            System.err.println("LOG INTEGRACAO ERRO: Falha ao chamar o Notificacao Service: " + e.getMessage());
        }
    }
}