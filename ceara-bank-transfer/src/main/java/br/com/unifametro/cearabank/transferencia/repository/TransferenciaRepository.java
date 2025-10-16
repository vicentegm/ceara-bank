package br.com.unifametro.cearabank.transferencia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.unifametro.cearabank.transferencia.enums.StatusTransferencia;
import br.com.unifametro.cearabank.transferencia.model.Transferencia;

@Repository
public interface TransferenciaRepository extends JpaRepository<Transferencia, String> {

    List<Transferencia> findByContaOrigemOrContaDestinoOrderByDataHoraDesc(String contaOrigem, String contaDestino);

    List<Transferencia> findByStatus(StatusTransferencia status);
}