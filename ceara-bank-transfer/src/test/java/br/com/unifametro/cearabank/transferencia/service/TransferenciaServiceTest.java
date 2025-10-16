package br.com.unifametro.cearabank.transferencia.service;

import br.com.unifametro.cearabank.transferencia.dto.TransferenciaRequestDTO;
import br.com.unifametro.cearabank.transferencia.dto.TransferenciaResponseDTO;
import br.com.unifametro.cearabank.transferencia.enums.StatusTransferencia;
import br.com.unifametro.cearabank.transferencia.enums.TipoTransferencia;
import br.com.unifametro.cearabank.transferencia.model.Transferencia;
import br.com.unifametro.cearabank.transferencia.repository.TransferenciaRepository;
import br.com.unifametro.cearabank.transferencia.service.TransferenciaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - TransferenciaService")
public class TransferenciaServiceTest {

    @InjectMocks // Injeta o Serviço a ser testado
    private TransferenciaService service;

    @Mock // Simula o repositório (DB)
    private TransferenciaRepository repository;

    private TransferenciaRequestDTO requestDTO;
    private Transferencia transferenciaSimulada;
    private final String ID_TESTE = "a1b2c3d4";

    @BeforeEach
    void setup() {
        // Inicializa DTO de requisição
        requestDTO = new TransferenciaRequestDTO();
        requestDTO.setContaOrigem("12345");
        requestDTO.setContaDestino("67890");
        requestDTO.setValor(new BigDecimal("100.00"));
        requestDTO.setTipo(TipoTransferencia.PIX);

        // Inicializa Model que seria salva/buscada
        transferenciaSimulada = new Transferencia();
        transferenciaSimulada.setId(ID_TESTE);
        transferenciaSimulada.setContaOrigem(requestDTO.getContaOrigem());
        transferenciaSimulada.setContaDestino(requestDTO.getContaDestino());
        transferenciaSimulada.setValor(requestDTO.getValor());
        transferenciaSimulada.setTipo(requestDTO.getTipo());
        transferenciaSimulada.setDataHora(LocalDateTime.now());
        transferenciaSimulada.setStatus(StatusTransferencia.SUCESSO);
    }

    @Test
    @DisplayName("1. Deve processar e salvar uma nova transferência com sucesso")
    void deveProcessarESalvarNovaTransferencia() {
        // 1. Configura o Mockito: Quando o save for chamado, retorne a simulação.
        when(repository.save(any(Transferencia.class))).thenReturn(transferenciaSimulada);

        // 2. Executa o método
        TransferenciaResponseDTO response = service.processarTransferencia(requestDTO);

        // 3. Verifica as Asserções
        assertNotNull(response);
        assertEquals(ID_TESTE, response.getIdTransacao());
        assertEquals(StatusTransferencia.SUCESSO.toString(), response.getStatus());
        assertEquals(TipoTransferencia.PIX, response.getTipo());
        
        // 4. Verifica se o método do repositório foi chamado exatamente uma vez
        verify(repository, times(1)).save(any(Transferencia.class));
    }

    @Test
    @DisplayName("2. Deve retornar a transferência ao consultar por ID")
    void deveConsultarStatusPorIdComSucesso() {
        // 1. Configura o Mockito: Quando o findById for chamado com o ID, retorne a transferência simulada
        when(repository.findById(ID_TESTE)).thenReturn(Optional.of(transferenciaSimulada));

        // 2. Executa o método
        Optional<TransferenciaResponseDTO> responseOpt = service.consultarStatus(ID_TESTE);

        // 3. Verifica as Asserções
        assertTrue(responseOpt.isPresent());
        assertEquals(ID_TESTE, responseOpt.get().getIdTransacao());
    }

    @Test
    @DisplayName("3. Deve retornar Optional vazio ao consultar um ID inexistente")
    void deveRetornarVazioAoConsultarIdInexistente() {
        // 1. Configura o Mockito: Retorna Optional vazio para qualquer ID.
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // 2. Executa o método
        Optional<TransferenciaResponseDTO> responseOpt = service.consultarStatus("id_que_nao_existe");

        // 3. Verifica as Asserções
        assertFalse(responseOpt.isPresent());
    }
    
    @Test
    @DisplayName("4. Deve listar o extrato de transferências de uma conta")
    void deveListarExtratoPorConta() {
        // 1. Cria uma lista simulada
        List<Transferencia> listaSimulada = List.of(transferenciaSimulada);

        // 2. Configura o Mockito
        when(repository.findByContaOrigemOrContaDestinoOrderByDataHoraDesc(anyString(), anyString()))
            .thenReturn(listaSimulada);

        // 3. Executa o método
        List<TransferenciaResponseDTO> extrato = service.listarExtratoConta("12345");

        // 4. Verifica as Asserções
        assertFalse(extrato.isEmpty());
        assertEquals(1, extrato.size());
        assertEquals("12345", extrato.get(0).getContaOrigem()); // <<<< LINHA CORRIGIDA
        
        // Verifica se o método customizado foi chamado
        verify(repository, times(1)).findByContaOrigemOrContaDestinoOrderByDataHoraDesc(eq("12345"), eq("12345"));
    }
}