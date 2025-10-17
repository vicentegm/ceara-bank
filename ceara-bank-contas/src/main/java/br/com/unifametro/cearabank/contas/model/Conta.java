package br.com.unifametro.cearabank.contas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma conta bancária.
 */
@Entity
@Data
public class Conta {
    
    // Usamos o username/identificador como ID único (chave primária)
    @Id
    private String id; // Ex: will.cearense

    private String cpfTitular;
    private String nomeTitular;
    private BigDecimal saldo;
    private LocalDateTime dataCriacao;
    
    public Conta() {
        this.dataCriacao = LocalDateTime.now();
        this.saldo = BigDecimal.ZERO;
    }
}
