package com.prosaude.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            .dataSimulacao(LocalDateTime.now())
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

      // Processa cada item da simulação
      for (SimulacaoRequest.ItemSimulacao item : itensRequest) {
          LocalDate dataAtual = item.getData();
          IndiceANS obterPercentualANS = obterPercentualANS(item.getData());
          if (primeiroItem) {
              // Primeira linha não sofre cálculos, é apenas adicionada
              valorAtual = item.getValor();
              valorANSAnterior = valorAtual; // Primeiro valor ANS igual ao valor do item
              primeiroItem = false;

              // Adiciona o primeiro item como está
              ItemSimulacao itemSimulacao = ItemSimulacao.builder()
                  .data(dataAtual)
                  .valor(valorAtual)
                  .tipoAumento(item.getTipoAumento())
                  .valorAumentoReal(BigDecimal.ZERO)
                  .percentualAumentoReal(BigDecimal.ZERO)
                  .percentualANS(BigDecimal.ZERO)
                  .indiceANS(null)  // Primeiro item não tem ANS calculado
                  .valorANS(valorAtual) // Valor ANS igual ao valor pago
                  .percentualDiferenca(BigDecimal.ZERO)
                  .simulacao(simulacao)
                  .cliente(cliente)
                  .build();

              itensCalculados.add(itemSimulacao);
              continue; // Passa para o próximo item sem fazer cálculos adicionais
          }

          // Para os itens subsequentes, calculamos os meses intermediários e valores de aumento
          BigDecimal valorAnterior = valorAtual;
          valorAtual = item.getValor();

          // Adiciona os meses intermediários entre a data anterior e a data atual
          if (!itensCalculados.isEmpty()) {
              ItemSimulacao ultimoItem = itensCalculados.get(itensCalculados.size() - 1);
              LocalDate dataAnterior = ultimoItem.getData();
              adicionarMesesIntermediarios(itensCalculados, dataAnterior, dataAtual, ultimoItem, simulacao, cliente, obterPercentualANS);
          }

          // Cálculos do aumento real e percentual
          BigDecimal valorAumentoReal = calcularAumentoReal(valorAnterior, valorAtual, primeiroItem);
          BigDecimal percentualAumentoReal = calcularPercentualAumentoReal(valorAnterior, valorAumentoReal);

          // Obter o percentual do índice ANS para a data do aumento
          
          BigDecimal percentualANS = obterPercentualANS.getPercentual();

          // Cálculo do valor ANS
          BigDecimal valorANS = calcularValorANS(valorANSAnterior, percentualANS);

          // Atualiza o valor ANS anterior para o próximo cálculo
          valorANSAnterior = valorANS;

          // Adiciona o ItemSimulacao à lista
          ItemSimulacao itemSimulacao = ItemSimulacao.builder()
              .data(dataAtual)
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

      // Após o último item, calculamos até a data de hoj
      ItemSimulacao ultimoItem = itensCalculados.get(itensCalculados.size() - 1);
      IndiceANS obterPercentualANS = obterPercentualANS(ultimoItem.getData());
      adicionarMesesIntermediarios(itensCalculados, ultimoItem.getData(), LocalDate.now(), ultimoItem, simulacao, cliente, obterPercentualANS);

      return itensCalculados;
  }

  private void adicionarMesesIntermediarios(List<ItemSimulacao> itensCalculados, LocalDate dataInicio, LocalDate dataFim, ItemSimulacao ultimoItem, Simulacao simulacao, Cliente cliente, IndiceANS obterPercentualANS) {
      // Calcula todos os meses intermediários entre as duas datas
      LocalDate dataAtual = dataInicio.plusMonths(1);

      while (dataAtual.isBefore(dataFim)) {
          // Cálculos do valor para o mês intermediário
          IndiceANS indiceANS = obterPercentualANS;
          BigDecimal percentualANS = indiceANS.getPercentual();
          BigDecimal valorANS = ultimoItem.getValorANS();

          // Adiciona o item para o mês intermediário
          ItemSimulacao itemSimulacao = ItemSimulacao.builder()
              .data(dataAtual)
              .valor(ultimoItem.getValor())
              .tipoAumento(TipoAumento.NENHUM)  // Defina um tipo adequado
              .valorAumentoReal(ultimoItem.getValorAumentoReal())  // Ajuste conforme necessidade
              .percentualAumentoReal(ultimoItem.getPercentualAumentoReal())  // Ajuste conforme necessidade
              .percentualANS(percentualANS)
              .indiceANS(indiceANS)
              .valorANS(valorANS)
              .percentualDiferenca(ultimoItem.getPercentualDiferenca())
              .simulacao(simulacao)
              .cliente(cliente)
              .build();

          itensCalculados.add(itemSimulacao);
          dataAtual = dataAtual.plusMonths(1);
      }
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

    public List<Simulacao> findAllSimulacoesComItens() {
        return simulacaoRepository.buscarSimulacoesComItens();
    }

    public IndiceANS obterPercentualANS(LocalDate data) {
        Optional<IndiceANS> indicePorData = indiceANSRepository.findIndicePorData(data);
        if (indicePorData.isEmpty()) return IndiceANS.builder().percentual(BigDecimal.ZERO).build();
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
