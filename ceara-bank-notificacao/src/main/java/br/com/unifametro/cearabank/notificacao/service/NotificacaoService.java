package br.com.unifametro.cearabank.notificacao.service;

import br.com.unifametro.cearabank.notificacao.dto.NotificacaoRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoServiceImpl.class);

    @Override
    @Async // Executa a lógica em uma thread separada
    public void processarENotificar(NotificacaoRequestDTO request) {
        logger.info("INÍCIO do processamento assíncrono para transação ID: {}", request.getIdTransacao());

        try {
            // SIMULAÇÃO: Simula o trabalho real de envio de e-mail/SMS/etc.
            Thread.sleep(5000); // 5 segundos de espera
            
            logger.info("SUCESSO: Notificação enviada para conta de origem {} e destino {} às {}", 
                request.getContaOrigem(), request.getContaDestino(), LocalDateTime.now());

            // TODO: Aqui seria a lógica de salvar o registro da notificação no banco de dados
            
        } catch (InterruptedException e) {
            // Se a thread for interrompida, restaura o estado de interrupção e loga o erro.
            Thread.currentThread().interrupt();
            logger.error("ERRO: Processamento interrompido para ID: {}", request.getIdTransacao(), e);
        } catch (Exception e) {
            logger.error("ERRO inesperado ao processar notificação para ID: {}", request.getIdTransacao(), e);
        }
    }
}
