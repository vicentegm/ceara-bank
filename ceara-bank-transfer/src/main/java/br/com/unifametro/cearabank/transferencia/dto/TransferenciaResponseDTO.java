package br.com.unifametro.cearabank.transferencia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.unifametro.cearabank.transferencia.enums.TipoTransferencia;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class TransferenciaResponseDTO {

    private String idTransacao;
    private String contaOrigem;
    private String contaDestino;
    private BigDecimal valor;
    private TipoTransferencia tipo; // PIX, TED, DOC
    private LocalDateTime dataHora;
    private String status; // SUCESSO, PENDENTE, FALHA
}