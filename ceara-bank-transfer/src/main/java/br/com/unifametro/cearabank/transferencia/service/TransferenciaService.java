package br.com.unifametro.cearabank.transferencia.service;

import br.com.unifametro.cearabank.transferencia.dto.TransferenciaRequestDTO;
import br.com.unifametro.cearabank.transferencia.dto.TransferenciaResponseDTO;
import br.com.unifametro.cearabank.transferencia.dto.contas.MovimentacaoRequestDTO; 
import br.com.unifametro.cearabank.transferencia.dto.contas.SaldoResponseDTO; 
import br.com.unifametro.cearabank.transferencia.enums.StatusTransferencia;
import br.com.unifametro.cearabank.transferencia.model.Transferencia;
import br.com.unifametro.cearabank.transferencia.repository.TransferenciaRepository;

import org.springframework.beans.factory.annotation.Value; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 
import org.springframework.web.client.RestClient; 
import org.springframework.web.client.HttpClientErrorException; 

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransferenciaService {

    private final TransferenciaRepository repository;
    private final RestClient contasRestClient; 
    private final NotificacaoIntegratorService notificacaoIntegratorService; 

    // Construtor para injeção de dependências
    public TransferenciaService(
            TransferenciaRepository repository, 
            NotificacaoIntegratorService notificacaoIntegratorService, 
            @Value("${microservicos.contas.url}") String contasServiceUrl) {
        
        this.repository = repository;
        this.notificacaoIntegratorService = notificacaoIntegratorService;
        
        // Inicializa o RestClient com a URL base do serviço de Contas
        this.contasRestClient = RestClient.builder()
                .baseUrl(contasServiceUrl)
                .build();
    }

    // --- MÉTODOS AUXILIARES ---

    private TransferenciaResponseDTO toDTO(Transferencia model) {
        return new TransferenciaResponseDTO(
                model.getId(),
                model.getContaOrigem(),
                model.getContaDestino(),
                model.getValor(),
                model.getTipo(),
                model.getDataHora(),
                model.getStatus().toString()
        );
    }
    
    // --- LÓGICA DE INTEGRAÇÃO (RESTCLIENT) ---
    
    /**
     * Busca o saldo do usuário remetente no microserviço de Contas.
     */
    protected BigDecimal consultarSaldo(String username) {
        try {
            SaldoResponseDTO response = contasRestClient.get()
                .uri("/saldo/{username}", username) // Ex: GET /api/v1/contas/saldo/will.cearense
                .retrieve()
                .body(SaldoResponseDTO.class);

            if (response != null && response.getSaldo() != null) {
                return response.getSaldo();
            }
            throw new IllegalStateException("Resposta de saldo nula ou inválida do serviço de contas.");
            
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Conta de origem não encontrada no sistema bancário.");
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao comunicar com o serviço de contas para consultar saldo: " + e.getMessage());
        }
    }
    
    /**
     * Realiza o débito na conta de origem e registra/encaminha a transação.
     */
    protected void realizarDebitoECredito(String remetenteUsername, TransferenciaRequestDTO request) {
        
        MovimentacaoRequestDTO movimentacao = new MovimentacaoRequestDTO(
            remetenteUsername,
            request.getValor(),
            request.getTipo().name() + "_DEBITO", // Ex: PIX_DEBITO
            request.getDescricao() != null ? request.getDescricao() : "Transferência " + request.getTipo().name()
        );

        try {
            contasRestClient.post()
                .uri("/debitar") 
                .body(movimentacao)
                .retrieve()
                .toBodilessEntity(); // Espera 200/204 de sucesso
                
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException("Falha na regra de negócio da transação (Serviço de Contas): " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalStateException("Falha crítica ao debitar e registrar a transação no serviço de contas: " + e.getMessage());
        }
    }

    // --- MÉTODOS DE NEGÓCIO PRINCIPAIS ---

    @Transactional
    public TransferenciaResponseDTO processarTransferencia(String remetenteUsername, TransferenciaRequestDTO dto) {
        
        // 1. INTEGRAÇÃO: Consultar Saldo (Validação de Saldo)
        BigDecimal saldoAtual = consultarSaldo(remetenteUsername); 

        if (saldoAtual.compareTo(dto.getValor()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar a transferência. Saldo atual: R$" + saldoAtual);
        }
        
        // 2. INTEGRAÇÃO: Realiza o Débito e Crédito
        realizarDebitoECredito(remetenteUsername, dto); 

        // 3. Cria e Salva a entidade Transferencia
        Transferencia novaTransf = new Transferencia();
        novaTransf.setContaOrigem(remetenteUsername); 
        novaTransf.setContaDestino(dto.getDocumentoDestinatario() + " - " + dto.getContaDestinatario()); 
        novaTransf.setValor(dto.getValor());
        novaTransf.setTipo(dto.getTipo());
        novaTransf.setDataHora(LocalDateTime.now());
        novaTransf.setStatus(StatusTransferencia.SUCESSO);
        
        // 4. Salva no banco de dados
        Transferencia salva = repository.save(novaTransf);
        
        // 5. NOVO: Chamada Assíncrona para Notificação
        notificacaoIntegratorService.notificarAssincronamente(salva); // Chama o serviço para enviar à fila
        
        // 6. Retorna o DTO
        return toDTO(salva);
    }
    
    // Método de Negócio 2: Consulta o status de uma transferência
    public Optional<TransferenciaResponseDTO> consultarStatus(String id) {
        return repository.findById(id).map(this::toDTO);
    }

    // Método de Negócio 3: Lista o extrato de transferências por conta
    public List<TransferenciaResponseDTO> listarExtratoConta(String conta) {
        List<Transferencia> transferencias = repository.findByContaOrigemOrContaDestinoOrderByDataHoraDesc(conta, conta);
        return transferencias.stream()
                             .map(this::toDTO)
                             .collect(Collectors.toList());
    }

    // Método de Negócio 4: Lista todas as transferências por status (Ex: PENDENTES)
    public List<TransferenciaResponseDTO> listarPorStatus(StatusTransferencia status) {
        List<Transferencia> transferencias = repository.findByStatus(status);
        return transferencias.stream()
                             .map(this::toDTO)
                             .collect(Collectors.toList());
    }

    // Método de Negócio 5: Estorna uma transferência (Atualiza status)
    @Transactional
    public Optional<TransferenciaResponseDTO> estornarTransferencia(String id) {
        Optional<Transferencia> transfOpt = repository.findById(id);
        
        if (transfOpt.isPresent()) {
            Transferencia transf = transfOpt.get();
            
            // *ATENÇÃO: A lógica de REVERSÃO no serviço de contas PRECISA SER ADICIONADA AQUI*
            
            transf.setStatus(StatusTransferencia.ESTORNADA);
            Transferencia salva = repository.save(transf);

            // NOVO: Notificar o Estorno
            notificacaoIntegratorService.notificarAssincronamente(salva); 

            return Optional.of(toDTO(salva));
        }
        return Optional.empty();
    }

    // Método de Negócio 6: Agenda uma transferência (Salva com status AGENDADA)
    @Transactional
    public TransferenciaResponseDTO agendarTransferencia(String remetenteUsername, TransferenciaRequestDTO dto, LocalDateTime dataAgendamento) {
        Transferencia agendada = new Transferencia();
        agendada.setContaOrigem(remetenteUsername); 
        agendada.setContaDestino(dto.getDocumentoDestinatario() + " - " + dto.getContaDestinatario());
        agendada.setValor(dto.getValor());
        agendada.setTipo(dto.getTipo());
        agendada.setDataHora(dataAgendamento);
        agendada.setStatus(StatusTransferencia.AGENDADA);
        
        Transferencia salva = repository.save(agendada);
        return toDTO(salva);
    }
    
    // Método de Negócio 7: Cancela uma transferência agendada
    @Transactional
    public boolean cancelarAgendamento(String id) {
        Optional<Transferencia> transfOpt = repository.findById(id);
        
        if (transfOpt.isPresent() && transfOpt.get().getStatus() == StatusTransferencia.AGENDADA) {
            repository.delete(transfOpt.get());
            return true;
        }
        return false;
    }

    // Método de Negócio 8: Consulta o resumo diário 
    public BigDecimal calcularTotalTransferidoNoDia(String data) {
        System.out.println("LOG TRANSFERENCIA: Consultando total transferido na data: " + data);
        return repository.calcularTotalTransferidoNoDia(data)
                .orElse(BigDecimal.ZERO); // Retorna zero se não houver transferências concluídas
    }
}
