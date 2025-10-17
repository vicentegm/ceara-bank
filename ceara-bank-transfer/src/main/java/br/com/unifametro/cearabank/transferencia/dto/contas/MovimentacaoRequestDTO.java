package br.com.unifametro.cearabank.transferencia.dto.contas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoRequestDTO {
    private String contaOrigem;
    private BigDecimal valor;
    private String tipoTransacao; // Ex: DEBITO_PIX
    private String descricao;
}