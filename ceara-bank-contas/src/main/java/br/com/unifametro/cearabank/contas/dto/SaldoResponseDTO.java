package br.com.unifametro.cearabank.contas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de resposta para a consulta de saldo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaldoResponseDTO {
    private BigDecimal saldo;
    private String titular; // Nome do titular da conta
}
