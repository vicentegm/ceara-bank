package br.com.unifametro.cearabank.transferencia.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import br.com.unifametro.cearabank.transferencia.enums.TipoTransferencia;


@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class TransferenciaRequestDTO {

    @NotBlank(message = "A conta de origem é obrigatória")
    private String contaOrigem;

    @NotBlank(message = "A conta de destino é obrigatória")
    private String contaDestino;

    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser positivo")
    private BigDecimal valor;

    @NotNull(message = "O tipo da transferência é obrigatório (PIX, TED, DOC)")
    private TipoTransferencia tipo;
    
}