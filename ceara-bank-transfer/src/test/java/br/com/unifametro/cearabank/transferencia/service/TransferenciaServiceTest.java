package br.com.unifametro.cearabank.transferencia.service;

import br.com.unifametro.cearabank.transferencia.dto.TransferenciaRequestDTO;
import br.com.unifametro.cearabank.transferencia.dto.TransferenciaResponseDTO;
import br.com.unifametro.cearabank.transferencia.enums.StatusTransferencia;
import br.com.unifametro.cearabank.transferencia.enums.TipoTransferencia;
import br.com.unifametro.cearabank.transferencia.model.Transferencia;
import br.com.unifametro.cearabank.transferencia.repository.TransferenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings; // NOVO IMPORT
import org.mockito.quality.Strictness; // NOVO IMPORT

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
@MockitoSettings(strictness = Strictness.LENIENT) // <--- AJUSTE PARA EVITAR UnnecessaryStubbing
@DisplayName("Testes Unitários - TransferenciaService")
public class TransferenciaServiceTest {

    @Spy 
    @InjectMocks // Injeta o Serviço a ser testado
    private TransferenciaService service;

    @Mock // Simula o repositório (DB)
    private TransferenciaRepository repository;

    private TransferenciaRequestDTO requestDTO;
    private Transferencia transferenciaSimulada;
    private final String ID_TESTE = "a1b2c3d4";
    private final String REMETENTE_USERNAME = "will.cearense";
    private final String DESTINO_DOC = "99988877766";
    private final String DESTINO_CONTA = "54321-Z";

    @BeforeEach
    void setup() {
        // Inicializa DTO de requisição com os NOVOS campos
        requestDTO = new TransferenciaRequestDTO();
        requestDTO.setDocumentoDestinatario(DESTINO_DOC);
        requestDTO.setCodigoBancoDestinatario("001");
        requestDTO.setAgenciaDestinatario("1234");
        requestDTO.setContaDestinatario(DESTINO_CONTA);
        requestDTO.setValor(new BigDecimal("100.00"));
        requestDTO.setTipo(TipoTransferencia.PIX);

        // Configura mocks para métodos internos (saldo e débito/crédito)
        // Requer que os métodos no Service sejam 'protected' (ou 'public')
        doReturn(new BigDecimal("1000.00")).when(service).consultarSaldo(anyString());
        doNothing().when(service).realizarDebitoECredito(anyString(), any(TransferenciaRequestDTO.class));


        // Inicializa Model que seria salva/buscada (A conta de origem vem do Token/USERNAME)
        transferenciaSimulada = new Transferencia();
        transferenciaSimulada.setId(ID_TESTE);
        transferenciaSimulada.setContaOrigem(REMETENTE_USERNAME); // Usa o username do Token
        transferenciaSimulada.setContaDestino(DESTINO_DOC + " - " + DESTINO_CONTA); // Novo formato de destino
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

        // 2. Executa o método (AGORA COM DOIS ARGUMENTOS!)
        TransferenciaResponseDTO response = service.processarTransferencia(REMETENTE_USERNAME, requestDTO);

        // 3. Verifica as Asserções
        assertNotNull(response);
        assertEquals(ID_TESTE, response.getIdTransacao());
        assertEquals(StatusTransferencia.SUCESSO.toString(), response.getStatus());
        assertEquals(TipoTransferencia.PIX, response.getTipo());
        assertEquals(REMETENTE_USERNAME, response.getContaOrigem()); 
        
        // 4. Verifica se os métodos foram chamados
        verify(repository, times(1)).save(any(Transferencia.class));
        verify(service, times(1)).realizarDebitoECredito(eq(REMETENTE_USERNAME), eq(requestDTO));
    }

    @Test
    @DisplayName("1.1. Deve lançar exceção quando o saldo for insuficiente")
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        // 1. Configura o Mockito: Simula saldo baixo (R$ 50.00)
        doReturn(new BigDecimal("50.00")).when(service).consultarSaldo(anyString());
        
        // 2. Executa e verifica se a exceção é lançada
        requestDTO.setValor(new BigDecimal("100.00")); // Valor de R$100.00
        
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            service.processarTransferencia(REMETENTE_USERNAME, requestDTO);
        });

        // 3. Verifica a mensagem e interações
        assertTrue(thrown.getMessage().contains("Saldo insuficiente"));
        verify(repository, never()).save(any(Transferencia.class));
        verify(service, never()).realizarDebitoECredito(anyString(), any(TransferenciaRequestDTO.class));
    }


    @Test
    @DisplayName("2. Deve retornar a transferência ao consultar por ID")
    void deveConsultarStatusPorIdComSucesso() {
        // 1. Configura o Mockito
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
        // 1. Configura o Mockito
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // 2. Executa o método
        Optional<TransferenciaResponseDTO> responseOpt = service.consultarStatus("id_que_nao_existe");

        // 3. Verifica as Asserções
        assertFalse(responseOpt.isPresent());
    }
    
    @Test
    @DisplayName("4. Deve listar o extrato de transferências de uma conta")
    void deveListarExtratoPorConta() {
        final String CONTA_EXTRATO = "extrato.ceara";
        
        // 1. Cria uma lista simulada
        transferenciaSimulada.setContaOrigem(CONTA_EXTRATO); // Garante que a contaOrigem é a conta buscada
        List<Transferencia> listaSimulada = List.of(transferenciaSimulada);

        // 2. Configura o Mockito
        when(repository.findByContaOrigemOrContaDestinoOrderByDataHoraDesc(anyString(), anyString()))
            .thenReturn(listaSimulada);

        // 3. Executa o método
        List<TransferenciaResponseDTO> extrato = service.listarExtratoConta(CONTA_EXTRATO);

        // 4. Verifica as Asserções
        assertFalse(extrato.isEmpty());
        assertEquals(1, extrato.size());
        assertEquals(CONTA_EXTRATO, extrato.get(0).getContaOrigem()); 
        
        // Verifica se o método customizado foi chamado com os argumentos corretos
        verify(repository, times(1)).findByContaOrigemOrContaDestinoOrderByDataHoraDesc(eq(CONTA_EXTRATO), eq(CONTA_EXTRATO));
    }
}