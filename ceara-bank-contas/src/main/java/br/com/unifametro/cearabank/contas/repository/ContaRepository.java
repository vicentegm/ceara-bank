package br.com.unifametro.cearabank.contas.repository;

import br.com.unifametro.cearabank.contas.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para operações de persistência da entidade Conta.
 */
public interface ContaRepository extends JpaRepository<Conta, String> {
    // Métodos findById, save, etc., são herdados automaticamente.
}
