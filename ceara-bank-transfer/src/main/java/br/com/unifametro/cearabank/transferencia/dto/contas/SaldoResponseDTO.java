package br.com.unifametro.cearabank.transferencia.dto.contas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaldoResponseDTO {
    private BigDecimal saldo;
    private String titular;
}