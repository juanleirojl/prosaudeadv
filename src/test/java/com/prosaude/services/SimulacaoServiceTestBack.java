//package com.prosaude.services;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.Mockito.*;
//
//import java.io.File;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.prosaude.controllers.requests.SimulacaoRequest;
//import com.prosaude.model.Cliente;
//import com.prosaude.model.IndiceANS;
//import com.prosaude.model.Simulacao;
//import com.prosaude.model.ItemSimulacao;
//import com.prosaude.repositories.ClienteRepository;
//import com.prosaude.repositories.IndiceANSRepository;
//import com.prosaude.repositories.SimulacaoPlanoRepository;
//import com.prosaude.repositories.SimulacaoRepository;
//
//@ExtendWith(MockitoExtension.class)
//class SimulacaoServiceTestBack {
//
//    @Mock
//    private ClienteRepository clienteRepository;
//
//    @Mock
//    private SimulacaoPlanoRepository simulacaoPlanoRepository;
//
//    @Mock
//    private SimulacaoRepository simulacaoRepository;
//
//    @Mock
//    private IndiceANSRepository indiceANSRepository;
//
//    @InjectMocks
//    private SimulacaoService simulacaoService;
//
//    @Captor
//    private ArgumentCaptor<List<ItemSimulacao>> planosCaptor;
//
//    private Cliente cliente;
//
//    @BeforeEach
//    void setUp() {
//        cliente = Cliente.builder()
//                .email("teste@email.com")
//                .nome("Cliente Teste")
//                .simulacoes(new ArrayList<>())
//                .build();
//    }
//
//    private SimulacaoRequest criarSimulacaoRequest() throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        File jsonFile = new File("src/test/resources/simulacao.json");
//        return objectMapper.readValue(jsonFile, SimulacaoRequest.class);
//    }
//
//    @Test
//    void testProcessarSimulacaoComAumentosValidos() throws IOException {
//        SimulacaoRequest request = criarSimulacaoRequest();
//
//        // Mock de cliente e índice
//        when(clienteRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(cliente));
//        when(indiceANSRepository.findIndicePorData(any(LocalDate.class)))
//                .thenReturn(Optional.of(new IndiceANS(1L, BigDecimal.valueOf(6.0), LocalDate.now(), LocalDate.now().plusYears(1))));
//
//        simulacaoService.processarSimulacao(request);
//
//        verify(simulacaoPlanoRepository, times(1)).saveAll(planosCaptor.capture());
//        verify(simulacaoRepository, times(1)).save(any(Simulacao.class));
//
//        List<ItemSimulacao> planosSalvos = planosCaptor.getValue();
//        assertEquals(5, planosSalvos.size());
//
//        // Verifique os cálculos de cada plano
//        ItemSimulacao primeiroPlano = planosSalvos.get(0);
//        assertEquals(BigDecimal.ZERO, primeiroPlano.getAumentoPlano()); // Primeiro item não tem aumento
//        assertEquals(BigDecimal.ZERO, primeiroPlano.getAumentoDevido());
//        assertEquals(BigDecimal.ZERO, primeiroPlano.getPercentualAumentoPlano());
//        assertEquals(BigDecimal.ZERO, primeiroPlano.getPercentualDiferenca());
//
//        ItemSimulacao segundoPlano = planosSalvos.get(1);
//        assertNotNull(segundoPlano.getAumentoPlano());
//        assertNotNull(segundoPlano.getAumentoDevido());
//        assertTrue(segundoPlano.getPercentualDiferenca().compareTo(BigDecimal.ZERO) >= 0);
//    }
//
//    @Test
//    void testProcessarSimulacaoSemIndiceANS() throws IOException {
//        SimulacaoRequest request = criarSimulacaoRequest();
//
//        // Mock de cliente e ausência de índice ANS
//        when(clienteRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(cliente));
//        when(indiceANSRepository.findIndicePorData(any(LocalDate.class))).thenReturn(Optional.empty());
//
//        simulacaoService.processarSimulacao(request);
//
//        verify(simulacaoPlanoRepository, times(1)).saveAll(planosCaptor.capture());
//        verify(simulacaoRepository, times(1)).save(any(Simulacao.class));
//
//        List<ItemSimulacao> planosSalvos = planosCaptor.getValue();
//        assertEquals(5, planosSalvos.size());
//
//        // Verifique que não houve aumento devido à ausência de índice ANS
//        for (ItemSimulacao plano : planosSalvos) {
//            assertEquals(BigDecimal.ZERO, plano.getAumentoDevido());
//            assertEquals(BigDecimal.ZERO, plano.getPercentualDiferenca());
//        }
//    }
//
//    @Test
//    void testProcessarSimulacaoComNovoCliente() throws IOException {
//        SimulacaoRequest request = criarSimulacaoRequest();
//
//        // Mock de cliente inexistente
//        when(clienteRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
//        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
//        when(indiceANSRepository.findIndicePorData(any(LocalDate.class)))
//                .thenReturn(Optional.of(new IndiceANS(1L, BigDecimal.valueOf(5.0), LocalDate.now(), LocalDate.now().plusYears(1))));
//
//        simulacaoService.processarSimulacao(request);
//
//        verify(clienteRepository, times(1)).save(any(Cliente.class));
//        verify(simulacaoPlanoRepository, times(1)).saveAll(planosCaptor.capture());
//        verify(simulacaoRepository, times(1)).save(any(Simulacao.class));
//
//        List<ItemSimulacao> planosSalvos = planosCaptor.getValue();
//        assertEquals(5, planosSalvos.size());
//    }
//}
