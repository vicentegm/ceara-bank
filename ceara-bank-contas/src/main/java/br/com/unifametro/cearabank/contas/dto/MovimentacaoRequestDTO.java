package br.com.unifametro.cearabank.contas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de requisição usado pelo microserviço de Transferência para solicitar um débito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoRequestDTO {
    private String contaOrigem;
    private BigDecimal valor;
    private String tipoTransacao; // Ex: PIX_DEBITO
    private String descricao;
}
