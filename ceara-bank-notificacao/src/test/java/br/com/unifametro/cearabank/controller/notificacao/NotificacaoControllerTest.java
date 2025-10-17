package br.com.unifametro.cearabank.notificacao.controller;

import br.com.unifametro.cearabank.notificacao.dto.NotificacaoRequestDTO;
import br.com.unifametro.cearabank.notificacao.service.NotificacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração do Controller de Notificação.
 * Usamos @WebMvcTest para isolar o teste na camada do Controller.
 * * NOTA: Adicionamos @ComponentScan para forçar o Spring a encontrar 
 * o Controller e Service dentro do pacote 'notificacao'.
 */
@WebMvcTest(
    controllers = NotificacaoController.class,
    // Garante que o Spring escaneie este pacote
    // Apenas escaneia o Controller, excluindo as configurações de segurança, etc.
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX, 
        pattern = "br\\.com\\.unifametro\\.cearabank\\..*Config.*"
    )
)
@ComponentScan(basePackages = "br.com.unifametro.cearabank.notificacao")
public class NotificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simula requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // Converte objetos Java para JSON (e vice-versa)

    // Mockamos o Service para evitar que a lógica real de Thread.sleep seja executada
    @MockBean
    private NotificacaoService notificacaoService;

    private NotificacaoRequestDTO testDto;

    @BeforeEach
    void setUp() {
        // Prepara um DTO de requisição de notificação simulado usando o padrão Builder
        testDto = NotificacaoRequestDTO.builder()
            .idTransacao("TRANS-20251017-001")
            .contaOrigem("will.cearense")
            .contaDestino("maxo.vehi")
            .valor(new BigDecimal("150.50"))
            .dataHora(LocalDateTime.now())
            .status("CONCLUIDA")
            .build();
    }

    /**
     * Testa se o endpoint retorna 202 ACCEPTED e chama o serviço corretamente.
     */
    @Test
    void shouldReturnAcceptedAndCallService() throws Exception {
        
        // Configura o mock para não fazer nada quando for chamado (o service é void)
        doNothing().when(notificacaoService).processarENotificar(any(NotificacaoRequestDTO.class));

        // 1. Executa o POST na URL do endpoint com o JSON do DTO
        mockMvc.perform(post("/notificacoes/enviar-para-fila")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
                
                // 2. Verifica se o status retornado é 202 Accepted
                .andExpect(status().isAccepted());

        // 3. Verifica se o método processarENotificar do service foi chamado exatamente 1 vez
        verify(notificacaoService, times(1)).processarENotificar(any(NotificacaoRequestDTO.class));
    }

    /**
     * Testa o caso em que o service lança uma exceção interna, mas o Controller
     * ainda deve retornar 202 ACCEPTED, pois a transação de Transferência foi aceita.
     */
    @Test
    void shouldReturnAcceptedEvenIfServiceThrowsException() throws Exception {
        
        // Simula uma falha interna no service
        doThrow(new RuntimeException("Simulação de falha de processamento")).when(notificacaoService).processarENotificar(any(NotificacaoRequestDTO.class));

        // O Controller, seguindo o padrão de "fire-and-forget", retorna 202 ACCEPTED.
        mockMvc.perform(post("/notificacoes/enviar-para-fila")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
                
                // O Controller deve retornar 202, ignorando (ou tratando) a falha do service
                .andExpect(status().isAccepted()); 

        // O Service ainda deve ter sido chamado para tentar o processamento
        verify(notificacaoService, times(1)).processarENotificar(any(NotificacaoRequestDTO.class));
    }
}
