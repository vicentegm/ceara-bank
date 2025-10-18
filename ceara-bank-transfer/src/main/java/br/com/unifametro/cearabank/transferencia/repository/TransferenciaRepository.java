package br.com.unifametro.cearabank.transferencia.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.unifametro.cearabank.transferencia.enums.StatusTransferencia;
import br.com.unifametro.cearabank.transferencia.model.Transferencia;

import org.springframework.data.repository.query.Param; // Não se esqueça de importar o @Param
import java.math.BigDecimal;
import java.util.Optional;

// ... outras importações

@Repository
public interface TransferenciaRepository extends JpaRepository<Transferencia, String> {

    // ... outros métodos que você já tinha

    /**
     * Calcula o valor total transferido com sucesso em uma determinada data.
     * @param dataString A data no formato String (ex: '2025-10-17').
     * @return O somatório dos valores.
     */
    @Query(value = "SELECT SUM(t.valor) FROM transferencia t WHERE t.data_transacao::date = :dataString::date AND t.status = 'CONCLUIDA'", nativeQuery = true)
    Optional<BigDecimal> calcularTotalTransferidoNoDia(@Param("dataString") String dataString);

}