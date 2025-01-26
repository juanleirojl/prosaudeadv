//package com.prosaude.services;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import com.prosaude.controllers.requests.SimulacaoRequest;
//import com.prosaude.model.Cliente;
//import com.prosaude.model.IndiceANS;
//import com.prosaude.model.ItemSimulacao;
//import com.prosaude.model.Simulacao;
//import com.prosaude.model.TipoAumento;
//import com.prosaude.repositories.ClienteRepository;
//import com.prosaude.repositories.IndiceANSRepository;
//import com.prosaude.repositories.SimulacaoRepository;
//
//@ExtendWith(MockitoExtension.class)
//public class SimulacaoServiceTest {
//
//  @InjectMocks
//  private SimulacaoService simulacaoService;
//
//  @Mock
//  private IndiceANSRepository indiceANSRepository;
//
//  @Mock
//  private SimulacaoRepository simulacaoRepository; 
//
//  @Mock
//  private ClienteRepository clienteRepository;
//    
//  @BeforeEach
//  void setUp() {
//    // Simulando o comportamento do índice ANS
//    Mockito.when(indiceANSRepository.findIndicePorData(Mockito.any(LocalDate.class)))
//      .thenAnswer(invocation -> {
//          LocalDate data = invocation.getArgument(0, LocalDate.class);
//          List<IndiceANS> indices = List.of(
//              new IndiceANS(1L, BigDecimal.valueOf(9.65), LocalDate.of(2014, 4, 1), LocalDate.of(2015, 3, 31)),
//              new IndiceANS(2L, BigDecimal.valueOf(13.55), LocalDate.of(2015, 4, 1), LocalDate.of(2016, 3, 31)),
//              new IndiceANS(3L, BigDecimal.valueOf(13.57), LocalDate.of(2016, 4, 1), LocalDate.of(2017, 3, 31)),
//              new IndiceANS(4L, BigDecimal.valueOf(13.55), LocalDate.of(2017, 4, 1), LocalDate.of(2018, 3, 31)),
//              new IndiceANS(5L, BigDecimal.valueOf(10.00), LocalDate.of(2018, 4, 1), LocalDate.of(2019, 3, 31)),
//              new IndiceANS(6L, BigDecimal.valueOf(7.35), LocalDate.of(2019, 4, 1), LocalDate.of(2020, 3, 31)),
//              new IndiceANS(7L, BigDecimal.valueOf(-8.19), LocalDate.of(2020, 4, 1), LocalDate.of(2021, 3, 31)),
//              new IndiceANS(8L, BigDecimal.valueOf(15.50), LocalDate.of(2021, 4, 1), LocalDate.of(2022, 3, 31)),
//              new IndiceANS(9L, BigDecimal.valueOf(10.26), LocalDate.of(2022, 4, 1), LocalDate.of(2023, 3, 31)),
//              new IndiceANS(10L, BigDecimal.valueOf(9.63), LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31)),
//              new IndiceANS(11L, BigDecimal.valueOf(6.91), LocalDate.of(2024, 4, 1), LocalDate.of(2025, 3, 31))
//          );
//          return indices.stream()
//              .filter(indice -> !data.isBefore(indice.getDataInicio()) && data.isBefore(indice.getDataFim()))
//              .findFirst();
//      });
//
//    Mockito.when(simulacaoRepository.save(Mockito.any(Simulacao.class)))
//      .thenAnswer(invocation -> invocation.getArgument(0)); 
//    
// // Dado um cliente simulado
//    Cliente cliente = new Cliente();
//    cliente.setNome("João");
//    cliente.setEmail("joao@example.com");
//    
//    when(clienteRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(cliente));
//
//  }
//  
//  @Test
//  void testCalculoComPercentualDiferenca() {
//      SimulacaoRequest.ItemSimulacao item1 = new SimulacaoRequest.ItemSimulacao(
//          LocalDate.of(2015, 1, 1), BigDecimal.valueOf(1000), TipoAumento.VALOR_INICIAL);
//      SimulacaoRequest.ItemSimulacao item2 = new SimulacaoRequest.ItemSimulacao(
//          LocalDate.of(2016, 1, 1), BigDecimal.valueOf(1500), TipoAumento.ANIVERSARIO_PLANO);
//      SimulacaoRequest.ItemSimulacao item3 = new SimulacaoRequest.ItemSimulacao(
//          LocalDate.of(2017, 1, 1), BigDecimal.valueOf(1800), TipoAumento.ANIVERSARIO_PLANO);
//      SimulacaoRequest.ItemSimulacao item4 = new SimulacaoRequest.ItemSimulacao(
//          LocalDate.of(2018, 10, 1), BigDecimal.valueOf(2400), TipoAumento.FAIXA_ETARIA);
//
//      SimulacaoRequest request = new SimulacaoRequest(
//          "João Silva", "joao@email.com", "123456789", LocalDate.of(1990, 5, 15),
//          List.of(item1, item2, item3, item4)
//      );
//
//      Simulacao simulacao = simulacaoService.salvarSimulacao(request);
//
//      // Verificações
//      assertEquals(new BigDecimal("91000"), simulacao.getValorTotalPago());
//      assertEquals(new BigDecimal("59890.63"), simulacao.getValorTotalANS());
//
//      // Verificar a diferença percentual
//      BigDecimal percentualDiferenca = simulacao.getValorTotalPago()
//              .subtract(simulacao.getValorTotalANS())
//              .divide(simulacao.getValorTotalANS(), 4, RoundingMode.HALF_UP)
//              .multiply(BigDecimal.valueOf(100));
//      assertEquals(new BigDecimal("51.94"), percentualDiferenca.setScale(2, RoundingMode.HALF_UP));
//
//      // Verificar percentual de aumento para o primeiro item
//      ItemSimulacao primeiroItem = simulacao.getItens().get(0);
//      assertEquals(new BigDecimal("0.00"), primeiroItem.getPercentualAumentoReal());
//      assertEquals(new BigDecimal("9.65"), primeiroItem.getPercentualANS()); // conforme o índice ANS de 2015
//      assertEquals(new BigDecimal("0.0000"), primeiroItem.getPercentualDiferenca());
//  }
//
//  @Test
//  void testCalculoComIndicesANS() {
//      SimulacaoRequest.ItemSimulacao item1 = new SimulacaoRequest.ItemSimulacao(
//          LocalDate.of(2015, 1, 1), BigDecimal.valueOf(1000), TipoAumento.VALOR_INICIAL);
//      SimulacaoRequest.ItemSimulacao item2 = new SimulacaoRequest.ItemSimulacao(
//          LocalDate.of(2016, 1, 1), BigDecimal.valueOf(1500), TipoAumento.ANIVERSARIO_PLANO);
//      SimulacaoRequest.ItemSimulacao item3 = new SimulacaoRequest.ItemSimulacao(
//          LocalDate.of(2017, 1, 1), BigDecimal.valueOf(1800), TipoAumento.ANIVERSARIO_PLANO);
//      SimulacaoRequest.ItemSimulacao item4 = new SimulacaoRequest.ItemSimulacao(
//          LocalDate.of(2018, 10, 1), BigDecimal.valueOf(2400), TipoAumento.FAIXA_ETARIA);
//
//      SimulacaoRequest request = new SimulacaoRequest(
//          "João Silva", "joao@email.com", "123456789", LocalDate.of(1990, 5, 15),
//          List.of(item1, item2, item3, item4)
//      );
//
//      Simulacao simulacao = simulacaoService.salvarSimulacao(request);
//
//      // Verificações
//      assertEquals(new BigDecimal("91000"), simulacao.getValorTotalPago()); 
//      assertEquals(new BigDecimal("59890.63"), simulacao.getValorTotalANS()); 
//  }
//
//
//  @Test
//  public void testSimulacaoComAumentosDiversos() {
//    // Cenários de teste com os índices reais
//    SimulacaoRequest request = SimulacaoRequest.builder()
//            .nome("João da Silva")
//            .email("joao.silva@email.com")
//            .telefone("11999999999")
//            .dataNascimento(LocalDate.of(1980, 1, 1))
//            .itens(Arrays.asList(
//                    SimulacaoRequest.ItemSimulacao.builder()
//                            .data(LocalDate.of(2015, 1, 1))
//                            .valor(BigDecimal.valueOf(1000))
//                            .tipoAumento(TipoAumento.VALOR_INICIAL)
//                            .build(),
//                    SimulacaoRequest.ItemSimulacao.builder()
//                            .data(LocalDate.of(2016, 1, 1))
//                            .valor(BigDecimal.valueOf(1500))
//                            .tipoAumento(TipoAumento.ANIVERSARIO_PLANO)
//                            .build(),
//                    SimulacaoRequest.ItemSimulacao.builder()
//                            .data(LocalDate.of(2017, 1, 1))
//                            .valor(BigDecimal.valueOf(1800))
//                            .tipoAumento(TipoAumento.ANIVERSARIO_PLANO)
//                            .build(),
//                    SimulacaoRequest.ItemSimulacao.builder()
//                            .data(LocalDate.of(2018, 10, 1))
//                            .valor(BigDecimal.valueOf(2400))
//                            .tipoAumento(TipoAumento.FAIXA_ETARIA)
//                            .build()
//            ))
//            .build();
//
//    // Execução da simulação
//    Simulacao simulacao = simulacaoService.salvarSimulacao(request);
//
//    // Verificações
//    assertNotNull(simulacao);
//    assertEquals(46, simulacao.getItens().size());
//
//    // Verificar valores
//    ItemSimulacao primeiroItem = simulacao.getItens().get(0);
//    assertEquals(BigDecimal.valueOf(1000), primeiroItem.getValor());
//    assertEquals(BigDecimal.ZERO, primeiroItem.getValorAumentoReal());
//    assertEquals(new BigDecimal("0.00"), primeiroItem.getPercentualAumentoReal());
//  }
//}
