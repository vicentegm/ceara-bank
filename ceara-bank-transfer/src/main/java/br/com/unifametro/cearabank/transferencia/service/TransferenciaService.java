package br.com.unifametro.cearabank.transferencia.service;

import br.com.unifametro.cearabank.transferencia.dto.TransferenciaRequestDTO;
import br.com.unifametro.cearabank.transferencia.dto.TransferenciaResponseDTO;
import br.com.unifametro.cearabank.transferencia.enums.StatusTransferencia;
import br.com.unifametro.cearabank.transferencia.model.Transferencia;
import br.com.unifametro.cearabank.transferencia.repository.TransferenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransferenciaService {

    @Autowired
    private TransferenciaRepository repository;

    // Converte Model para DTO (Ajuda a manter a camada de Controller limpa)
    private TransferenciaResponseDTO toDTO(Transferencia model) {
        return new TransferenciaResponseDTO(
                model.getId(),
                model.getContaOrigem(),
                model.getContaDestino(),
                model.getValor(),
                model.getTipo(),
                model.getDataHora(),
                model.getStatus().toString() // Converte ENUM Status para String na resposta
        );
    }

    // Método de Negócio 1: Inicia uma nova transferência (Endpoint Principal)
    public TransferenciaResponseDTO processarTransferencia(TransferenciaRequestDTO dto) {
        // [Aqui viriam as validações de saldo e comunicação com o serviço de segurança/contas]
        
        // 1. Cria a entidade Transferencia a partir do DTO
        Transferencia novaTransf = new Transferencia();
        novaTransf.setContaOrigem(dto.getContaOrigem());
        novaTransf.setContaDestino(dto.getContaDestino());
        novaTransf.setValor(dto.getValor());
        novaTransf.setTipo(dto.getTipo()); // Tipo (ENUM) já vem no DTO
        novaTransf.setDataHora(LocalDateTime.now());
        
        // 2. Define o status inicial (Assumindo sucesso imediato para simplificação)
        novaTransf.setStatus(StatusTransferencia.SUCESSO);
        
        // 3. Salva no banco de dados
        Transferencia salva = repository.save(novaTransf);
        
        // 4. Retorna o objeto mapeado para o DTO
        return toDTO(salva);
    }
    
    // Método de Negócio 2: Consulta o status de uma transferência
    public Optional<TransferenciaResponseDTO> consultarStatus(String id) {
        return repository.findById(id).map(this::toDTO);
    }

    // Método de Negócio 3: Lista o extrato de transferências por conta
    public List<TransferenciaResponseDTO> listarExtratoConta(String conta) {
        // Usa o método customizado do Repository
        List<Transferencia> transferencias = repository.findByContaOrigemOrContaDestinoOrderByDataHoraDesc(conta, conta);
        
        // Mapeia a lista de Model para DTO
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
    public Optional<TransferenciaResponseDTO> estornarTransferencia(String id) {
        Optional<Transferencia> transfOpt = repository.findById(id);
        
        if (transfOpt.isPresent()) {
            Transferencia transf = transfOpt.get();
            // Lógica de estorno: Reverter saldo no Service de Contas e atualizar status
            transf.setStatus(StatusTransferencia.ESTORNADA);
            Transferencia salva = repository.save(transf);
            return Optional.of(toDTO(salva));
        }
        return Optional.empty();
    }

    // Método de Negócio 6: Agenda uma transferência (Salva com status AGENDADA)
    public TransferenciaResponseDTO agendarTransferencia(TransferenciaRequestDTO dto, LocalDateTime dataAgendamento) {
        // Cria e salva a entidade com status de AGENDADA
        Transferencia agendada = new Transferencia();
        agendada.setContaOrigem(dto.getContaOrigem());
        agendada.setContaDestino(dto.getContaDestino());
        agendada.setValor(dto.getValor());
        agendada.setTipo(dto.getTipo());
        agendada.setDataHora(dataAgendamento); // Usa a data futura
        agendada.setStatus(StatusTransferencia.AGENDADA);
        
        Transferencia salva = repository.save(agendada);
        return toDTO(salva);
    }

    // Método de Negócio 7: Cancela uma transferência agendada
    public boolean cancelarAgendamento(String id) {
        Optional<Transferencia> transfOpt = repository.findById(id);
        
        if (transfOpt.isPresent() && transfOpt.get().getStatus() == StatusTransferencia.AGENDADA) {
            repository.delete(transfOpt.get());
            return true;
        }
        return false;
    }

    // Método de Negócio 8: Consulta o resumo diário (Simulação)
    public BigDecimal calcularTotalTransferidoNoDia(String data) {
        // Em um projeto real, você usaria um método no Repository para somar os valores
        // onde a dataHora está entre 00:00 e 23:59 da data informada e o status é SUCESSO.
        
        // Simulação com valor estático, pois a query JPA real é mais complexa
        return new BigDecimal("1500000.00");
    }
}