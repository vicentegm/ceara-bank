package br.com.unifametro.cearabank.contas.service;

import br.com.unifametro.cearabank.contas.dto.MovimentacaoRequestDTO;
import br.com.unifametro.cearabank.contas.dto.SaldoResponseDTO;
import br.com.unifametro.cearabank.contas.model.Conta;
import br.com.unifametro.cearabank.contas.repository.ContaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContaServiceTest {

    private ContaRepository repository;
    private ContaService service;

    @BeforeEach
    void setUp() {
        repository = mock(ContaRepository.class);
        service = new ContaService(repository);
    }

    @Test
    void consultarSaldo_deveRetornarSaldoQuandoContaExiste() {
        Conta conta = new Conta();
        conta.setId("123");
        conta.setSaldo(new BigDecimal("1000"));
        conta.setNomeTitular("João");

        when(repository.findById("123")).thenReturn(Optional.of(conta));

        SaldoResponseDTO response = service.consultarSaldo("123");

        assertEquals(new BigDecimal("1000"), response.getSaldo());
        assertEquals("João", response.getTitular());
    }

    @Test
    void consultarSaldo_deveLancarExcecaoQuandoContaNaoExiste() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.consultarSaldo("999"));

        assertEquals("Conta não encontrada.", exception.getMessage());
    }

    @Test
    void processarDebito_deveDebitarValorQuandoSaldoSuficiente() {
        Conta conta = new Conta();
        conta.setId("abc");
        conta.setSaldo(new BigDecimal("500"));

        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO();
        request.setContaOrigem("abc");
        request.setValor(new BigDecimal("200"));
        request.setTipoTransacao("TRANSFERENCIA");

        when(repository.findById("abc")).thenReturn(Optional.of(conta));

        service.processarDebito(request);

        assertEquals(new BigDecimal("300"), conta.getSaldo());
        verify(repository).save(conta);
    }

    @Test
    void processarDebito_deveLancarExcecaoQuandoContaNaoExiste() {
        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO();
        request.setContaOrigem("inexistente");
        request.setValor(new BigDecimal("100"));

        when(repository.findById("inexistente")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.processarDebito(request));

        assertEquals("Conta de origem não encontrada.", exception.getMessage());
    }

    @Test
    void processarDebito_deveLancarExcecaoQuandoSaldoInsuficiente() {
        Conta conta = new Conta();
        conta.setId("abc");
        conta.setSaldo(new BigDecimal("100"));

        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO();
        request.setContaOrigem("abc");
        request.setValor(new BigDecimal("200"));

        when(repository.findById("abc")).thenReturn(Optional.of(conta));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.processarDebito(request));

        assertEquals("Saldo insuficiente para o débito.", exception.getMessage());
    }

    @Test
    void criarContaTeste_deveCriarContaQuandoNaoExiste() {
        when(repository.existsById("nova")).thenReturn(false);

        service.criarContaTeste("nova", new BigDecimal("1000"));

        ArgumentCaptor<Conta> captor = ArgumentCaptor.forClass(Conta.class);
        verify(repository).save(captor.capture());

        Conta contaSalva = captor.getValue();
        assertEquals("nova", contaSalva.getId());
        assertEquals(new BigDecimal("1000"), contaSalva.getSaldo());
        assertEquals("Aluno Teste nova", contaSalva.getNomeTitular());
        assertEquals("00000000000", contaSalva.getCpfTitular());
    }

    @Test
    void criarContaTeste_naoDeveCriarContaSeJaExiste() {
        when(repository.existsById("existente")).thenReturn(true);

        service.criarContaTeste("existente", new BigDecimal("5000"));

        verify(repository, never()).save(any());
    }
}
