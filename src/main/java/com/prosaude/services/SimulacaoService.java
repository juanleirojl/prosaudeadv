package com.prosaude.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.prosaude.controllers.requests.SimulacaoRequest;
import com.prosaude.model.Cliente;
import com.prosaude.model.IndiceANS;
import com.prosaude.model.ItemSimulacao;
import com.prosaude.model.Simulacao;
import com.prosaude.model.TipoAumento;
import com.prosaude.repositories.ClienteRepository;
import com.prosaude.repositories.IndiceANSRepository;
import com.prosaude.repositories.SimulacaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimulacaoService {

    private final SimulacaoRepository simulacaoRepository;
    private final IndiceANSRepository indiceANSRepository;
    private final ClienteRepository clienteRepository;

    public Simulacao salvarSimulacao(SimulacaoRequest request) {
      
      Cliente cliente = recuperaOuCriaCliente(request);
      
        Simulacao simulacao = Simulacao.builder()
            .cliente(cliente)
            .dataSimulacao(LocalDate.now())
                .build();

        List<ItemSimulacao> itens = calcularValoresSimulacao(request.getItens(), simulacao, cliente);

        simulacao.setItens(itens);
        simulacao.setValorTotalPago(itens.stream().map(ItemSimulacao::getValor).reduce(BigDecimal.ZERO, BigDecimal::add));
        simulacao.setValorTotalANS(itens.stream().map(ItemSimulacao::getValorANS).reduce(BigDecimal.ZERO, BigDecimal::add));
        simulacao.setPercentualDiferenca(calcularPercentualDiferenca(simulacao.getValorTotalPago(), simulacao.getValorTotalANS()));
        
        return simulacaoRepository.save(simulacao);
    }

    private List<ItemSimulacao> calcularValoresSimulacao(List<SimulacaoRequest.ItemSimulacao> itensRequest, Simulacao simulacao, Cliente cliente) {
      BigDecimal valorAtual = BigDecimal.ZERO;
      BigDecimal valorANSAnterior = BigDecimal.ZERO;
      List<ItemSimulacao> itensCalculados = new ArrayList<>();
      boolean primeiroItem = true;

      for (SimulacaoRequest.ItemSimulacao item : itensRequest) {
          BigDecimal valorAnterior = valorAtual;
          valorAtual = item.getValor();

          // Cálculos do aumento real e percentual
          BigDecimal valorAumentoReal = calcularAumentoReal(valorAnterior, valorAtual, primeiroItem);
          BigDecimal percentualAumentoReal = calcularPercentualAumentoReal(valorAnterior, valorAumentoReal);
          
          // Obter o percentual do índice ANS para a data do aumento
            IndiceANS obterPercentualANS = obterPercentualANS(item.getData());
            BigDecimal percentualANS = obterPercentualANS.getPercentual();

          // Cálculo do valor ANS: para o primeiro item, o valor ANS será igual ao valor pago
          BigDecimal valorANS = primeiroItem ? valorAtual : calcularValorANS(valorANSAnterior, percentualANS);

          // Atualiza o valor considerando o aumento de faixa etária
//          if (item.getTipoAumento() == TipoAumento.FAIXA_ETARIA && !primeiroItem) {
//              valorAtual = aplicarLimiteFaixaEtaria(valorAnterior, valorAtual);
//              valorAumentoReal = valorAtual.subtract(valorAnterior);
//              percentualAumentoReal = calcularPercentualAumentoReal(valorAnterior, valorAumentoReal);
//          }

          // Marca o primeiro item como processado
          primeiroItem = false;

          // Atualiza o valor ANS anterior para o próximo cálculo
          valorANSAnterior = valorANS;
          
          // Adiciona o ItemSimulacao à lista
          ItemSimulacao itemSimulacao = ItemSimulacao.builder()
              .data(item.getData())
              .valor(valorAtual)
              .tipoAumento(item.getTipoAumento())
              .valorAumentoReal(valorAumentoReal)
              .percentualAumentoReal(percentualAumentoReal)
              .percentualANS(percentualANS)
              .indiceANS(obterPercentualANS)
              .valorANS(valorANS)
              .percentualDiferenca(calcularPercentualDiferenca(valorAtual, valorANS))
              .simulacao(simulacao)
              .cliente(cliente)
              .build();


          itensCalculados.add(itemSimulacao);
      }

      return itensCalculados;
  }

    private BigDecimal calcularPercentualDiferenca(BigDecimal valorPago, BigDecimal valorANS) {
      if (valorANS.compareTo(BigDecimal.ZERO) > 0) {
          return valorPago.subtract(valorANS).divide(valorANS, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
      }
      return BigDecimal.ZERO;
  }

    private BigDecimal calcularAumentoReal(BigDecimal valorAnterior, BigDecimal valorAtual, boolean primeiroItem) {
      return primeiroItem ? BigDecimal.ZERO : valorAtual.subtract(valorAnterior).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calcularPercentualAumentoReal(BigDecimal valorAnterior, BigDecimal valorAumentoReal) {
      return valorAnterior.compareTo(BigDecimal.ZERO) > 0
              ? valorAumentoReal.divide(valorAnterior, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
              : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calcularValorANS(BigDecimal valorAnteriorANS, BigDecimal percentualANS) {
      return valorAnteriorANS.multiply(BigDecimal.ONE.add(percentualANS.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
              .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal aplicarLimiteFaixaEtaria(BigDecimal valorAnterior, BigDecimal valorAtual) {
      BigDecimal valorMaximoFaixa = valorAnterior.multiply(BigDecimal.valueOf(1.30));
      return valorAtual.min(valorMaximoFaixa).setScale(2, RoundingMode.HALF_UP);
  }


    public List<Simulacao> obterSimulacoes() {
      return simulacaoRepository.findAll();
    }
    
    public IndiceANS obterPercentualANS(LocalDate data) {
        Optional<IndiceANS> indicePorData = indiceANSRepository.findIndicePorData(data);
        if(indicePorData.isEmpty()) return IndiceANS.builder().percentual(BigDecimal.ZERO).build();
        return indicePorData.get();
    }

    public Simulacao obterSimulacaoPorId(Long id) {
      return simulacaoRepository.findById(id).orElseThrow(() -> new RuntimeException("Detalhe não encontrado: " + id));
    }
    
    private Cliente recuperaOuCriaCliente(SimulacaoRequest request) {
      // Primeiro tenta encontrar o cliente pelo email
      Cliente cliente = clienteRepository.findByEmail(request.getEmail())
          .orElseGet(() -> {
              // Se não encontrar pelo email, tenta pelo telefone
              if (request.getTelefone() != null && !request.getTelefone().isEmpty()) {
                  return clienteRepository.findByTelefone(request.getTelefone())
                      .orElseGet(() -> {
                          // Se não encontrar pelo telefone, cria um novo cliente
                          Cliente novoCliente = new Cliente();
                          novoCliente.setNome(request.getNome());
                          novoCliente.setEmail(request.getEmail());
                          novoCliente.setTelefone(request.getTelefone());
                          novoCliente.setDataNascimento(request.getDataNascimento());
                          return clienteRepository.save(novoCliente);
                      });
              } else {
                  // Se não encontrar pelo email e telefone não for informado, cria um novo cliente
                  Cliente novoCliente = new Cliente();
                  novoCliente.setNome(request.getNome());
                  novoCliente.setEmail(request.getEmail());
                  novoCliente.setTelefone(request.getTelefone());
                  novoCliente.setDataNascimento(request.getDataNascimento());
                  return clienteRepository.save(novoCliente);
              }
          });
      return cliente;
    }


}
