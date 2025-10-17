package br.com.unifametro.cearabank.notificacao.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para receber os dados de notificação do microserviço Transferências via Fila (RabbitMQ).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoRequestDTO {
    
    // ID da transação gerada pelo serviço de Transferências
    private Long idTransacao;
    
    // ID da conta de origem (username)
    private String contaOrigemId;
    
    // ID da conta de destino (username)
    private String contaDestinoId;

    // Valor da transação
    private BigDecimal valor;

    // Data da transação
    private LocalDateTime dataTransacao;

    // Status da transação (ex: CONCLUIDA, FALHA_SALDO)
    private String status;
}