package br.com.unifametro.cearabank.transferencia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.unifametro.cearabank.transferencia.enums.StatusTransferencia;
import br.com.unifametro.cearabank.transferencia.enums.TipoTransferencia;

@Entity 
@Table(name = "transferencias")
@Data 
@NoArgsConstructor 
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // ID único e gerado automaticamente
    private String id;

    @Column(nullable = false)
    private String contaOrigem;

    @Column(nullable = false)
    private String contaDestino;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING) // Grava o nome do ENUM (ex: 'PIX')
    @Column(nullable = false)
    private TipoTransferencia tipo;
    
    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING) // Grava o nome do ENUM (ex: 'SUCESSO')
    @Column(nullable = false)
    private StatusTransferencia status; 
}