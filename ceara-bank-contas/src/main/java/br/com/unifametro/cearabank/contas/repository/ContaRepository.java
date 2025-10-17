package br.com.unifametro.cearabank.contas.repository;

import br.com.unifametro.cearabank.contas.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface Repository para operações de CRUD da entidade Conta, usando String (Username/ID) como chave primária.
 */
@Repository
public interface ContaRepository extends JpaRepository<Conta, String> {
    // A chave primária é String, então herdamos findById(String id).
}
