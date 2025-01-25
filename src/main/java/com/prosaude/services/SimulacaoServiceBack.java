package com.prosaude.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.prosaude.repositories.ClienteRepository;
import com.prosaude.repositories.IndiceANSRepository;
import com.prosaude.repositories.SimulacaoPlanoRepository;
import com.prosaude.repositories.SimulacaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimulacaoServiceBack {

    private final ClienteRepository clienteRepository;
    private final SimulacaoPlanoRepository simulacaoPlanoRepository;
    private final SimulacaoRepository simulacaoRepository;
    private final IndiceANSRepository indiceANSRepository;

    @Transactional
    public Simulacao processarSimulacao(SimulacaoRequest request) {
        Cliente cliente = recuperaOuCriaCliente(request);

        BigDecimal valorAnterior = null;

        // Variáveis para acumular os totais
        BigDecimal totalPago = BigDecimal.ZERO;
        BigDecimal totalDiferenca = BigDecimal.ZERO;

        // Lista para armazenar os planos da simulação
        List<ItemSimulacao> simulacoesPlano = new ArrayList<>();

        for (SimulacaoRequest.ItemSimulacao item : request.getItens()) {
            // Busca o índice de reajuste da ANS para o mês atual
            Optional<IndiceANS> indiceANSOpt = indiceANSRepository.findIndicePorData(item.getData());
            BigDecimal percentualIndice = indiceANSOpt.map(IndiceANS::getPercentual).orElse(BigDecimal.ZERO);

            // Inicializar valores padrão
            BigDecimal aumentoPlano = BigDecimal.ZERO;
            BigDecimal aumentoDevido = BigDecimal.ZERO;
            BigDecimal percentualAumentoPlano = BigDecimal.ZERO;
            BigDecimal percentualDiferenca = BigDecimal.ZERO;
            BigDecimal valorDevidoAtual = valorAnterior != null ? valorAnterior : item.getValor(); // Inicializa com o valor anterior ou o valor atual, caso seja o primeiro item.

            // Calcula valores se houver valor anterior e o índice for válido
            if (valorAnterior != null && percentualIndice.abs() != BigDecimal.ZERO) {
                aumentoPlano = item.getValor().subtract(valorAnterior); // Aumento dado pelo plano
                
             // Cálculo correto do aumento devido pela ANS
                aumentoDevido = valorDevidoAtual.multiply(percentualIndice)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                
                valorDevidoAtual = valorDevidoAtual.add(aumentoDevido); // Atualiza o valor devido acumulado

                // Percentual de aumento do plano
                percentualAumentoPlano = aumentoPlano.divide(valorAnterior, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

                // Percentual da diferença entre o aumento devido e o plano
                percentualDiferenca = aumentoPlano.subtract(aumentoDevido)
                        .divide(valorAnterior, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }else {
              valorDevidoAtual = item.getValor();
            }

            // Criação do simulacaoPlano
            ItemSimulacao simulacaoPlano = ItemSimulacao.builder()
//                .valor(item.getValor())
//                .data(item.getData())
//                .tipoAumento(item.getTipoAumento())
//                .cliente(cliente)
//                .aumentoPlano(aumentoPlano) // Guarda o aumento dado pelo plano
//                .aumentoDevido(aumentoDevido) // Guarda o aumento devido pela ANS
//                .percentualAumentoPlano(percentualAumentoPlano) // Percentual de aumento dado pelo plano
//                .percentualDiferenca(percentualDiferenca) // Percentual de diferença entre aumento dado e devido
//                .indiceANS(indiceANSOpt.isPresent() ? indiceANSOpt.get() : null)
                .build();

            // Adiciona o simulacaoPlano à lista
            simulacoesPlano.add(simulacaoPlano);

            // Atualiza os totais
            totalPago = totalPago.add(item.getValor());
           // totalDevido = totalDevido.add(valorDevidoAtual);
            totalDiferenca = totalDiferenca.add(aumentoPlano.subtract(aumentoDevido));

            valorAnterior = item.getValor();
        }

        // Calcular o percentual de diferença entre o total pago e o total devido
        BigDecimal percentualTotalDiferenca = BigDecimal.ZERO;
//        if (totalDevido.compareTo(BigDecimal.ZERO) > 0) {
//            percentualTotalDiferenca = totalDiferenca.divide(totalDevido, 4, RoundingMode.HALF_UP)
//                .multiply(BigDecimal.valueOf(100))
//                .setScale(2, RoundingMode.HALF_UP);
//        }

        // Criação da Simulacao (entidade pai)
        Simulacao simulacao = Simulacao.builder()
            .dataSimulacao(LocalDateTime.now())  // Data da simulação
            .cliente(cliente)
            //.simulacoesPlano(simulacoesPlano)  // Lista de planos da simulação
            //.totalPago(totalPago)  // Total pago
           // .totalDevido(totalDevido)  // Total devido pela ANS
           // .percentualDiferenca(percentualTotalDiferenca)  // Percentual de diferença
            .build();

        // Antes de salvar a simulacao, associamos o simulacaoPlano à simulação
        for (ItemSimulacao simulacaoPlano : simulacoesPlano) {
            simulacaoPlano.setSimulacao(simulacao);
        }

        // Salva a simulação no banco
        simulacaoPlanoRepository.saveAll(simulacoesPlano);  // Salva todos os planos associados
        simulacaoRepository.save(simulacao);  // Salva a simulação principal
        
        return simulacao;
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


    public List<Simulacao> obterSimulacoes() {
      return simulacaoRepository.findAll();
    }

    public Simulacao obterSimulacaoPorId(Long id) {
      return simulacaoRepository.findById(id).orElseThrow(() -> new RuntimeException("Detalhe não encontrado: " + id));
    }


}
