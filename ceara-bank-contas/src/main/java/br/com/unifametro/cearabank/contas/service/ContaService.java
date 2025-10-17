package br.com.unifametro.cearabank.contas.service;

import br.com.unifametro.cearabank.contas.dto.MovimentacaoRequestDTO;
import br.com.unifametro.cearabank.contas.dto.SaldoResponseDTO;
import br.com.unifametro.cearabank.contas.model.Conta;
import br.com.unifametro.cearabank.contas.repository.ContaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Camada de serviço responsável pela lógica de Contas e Saldos.
 */
@Service
public class ContaService {

    private final ContaRepository repository;

    public ContaService(ContaRepository repository) {
        this.repository = repository;
    }

    /**
     * Retorna o saldo e o nome do titular da conta.
     * @param contaId O ID da conta (username).
     * @return DTO com saldo e titular.
     * @throws IllegalArgumentException se a conta não for encontrada.
     */
    public SaldoResponseDTO consultarSaldo(String contaId) {
        // Busca a conta pelo ID e lança exceção se não existir
        Conta conta = repository.findById(contaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        return new SaldoResponseDTO(conta.getSaldo(), conta.getNomeTitular());
    }

    /**
     * Processa a movimentação de débito de forma atômica e transacional.
     * @param request DTO com informações da movimentação.
     * @throws IllegalArgumentException se a conta não for encontrada ou saldo for insuficiente.
     */
    @Transactional
    public void processarDebito(MovimentacaoRequestDTO request) {
        Conta conta = repository.findById(request.getContaOrigem())
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada."));

        // 1. Validação de Saldo
        if (conta.getSaldo().compareTo(request.getValor()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para o débito.");
        }

        // 2. Realiza o Débito
        conta.setSaldo(conta.getSaldo().subtract(request.getValor()));
        
        System.out.println("LOG CONTAS: Débito de R$" + request.getValor() + 
                           " da conta " + conta.getId() + " processado. " +
                           "Tipo: " + request.getTipoTransacao() + ".");

        // 3. Salva a alteração
        repository.save(conta);
    }
    
    /**
     * Método utilitário para criar contas iniciais para testes rápidos.
     */
    @Transactional
    public void criarContaTeste(String id, BigDecimal saldoInicial) {
        if (repository.existsById(id)) return; // Evita duplicidade

        Conta conta = new Conta();
        conta.setId(id);
        conta.setSaldo(saldoInicial);
        conta.setNomeTitular("Aluno Teste " + id);
        conta.setCpfTitular("00000000000"); 
        repository.save(conta);
    }
}