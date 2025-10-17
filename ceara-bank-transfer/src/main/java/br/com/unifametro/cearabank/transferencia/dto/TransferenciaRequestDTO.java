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

    @NotBlank(message = "O CPF/CNPJ do destinatário é obrigatório.")
    private String documentoDestinatario; // CPF ou CNPJ do destinatário

    @NotBlank(message = "O código do banco destinatário é obrigatório.")
    private String codigoBancoDestinatario; // Ex: 001 (BB), 104 (Caixa), etc.
    
    @NotBlank(message = "O número da agência do destinatário é obrigatório.")
    private String agenciaDestinatario;

    @NotBlank(message = "O número da conta do destinatário é obrigatório.")
    private String contaDestinatario;

    @NotNull(message = "O valor da transferência é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor da transferência deve ser positivo.")
    private BigDecimal valor;

    private String descricao;

    @NotNull(message = "O tipo da transferência é obrigatório (PIX, TED, DOC)")
    private TipoTransferencia tipo;
    
}