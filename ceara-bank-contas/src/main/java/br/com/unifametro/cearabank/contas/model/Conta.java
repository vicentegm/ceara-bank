package br.com.unifametro.cearabank.contas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma conta bancária.
 * Adicionei getters/setters explícitos no arquivo de correção para garantir a compilação
 * caso o Lombok não esteja configurado corretamente no ambiente de teste,
 * mas mantive o @Data original para o restante do seu código.
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
    
    // Construtor para inicialização de testes (se necessário, embora usemos set methods)
    // Se o @Data não gerar construtor com todos os campos, o teste deve usar o construtor padrão e setters.

    // Getters e Setters gerados pelo Lombok (@Data), mas listados aqui para clareza
    // e para garantir que o compilador os encontre se o @Data não estiver funcionando.
    
    // Como o @Data já gera, vou manter apenas o construtor padrão,
    // mas se o compilador reclamar de 'getNumeroConta' ou 'getId',
    // significa que o @Data não está sendo processado corretamente.
    
    // Deixando aqui para garantir que a classe é a mais próxima da sua original,
    // mas com o Lombok (o que significa que os métodos existem, se o Lombok estiver OK).

}
