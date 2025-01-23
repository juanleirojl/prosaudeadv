package com.prosaude.controllers;

/*
 * <td>
                                <button class="btn btn-primary btn-sm" onclick="event.stopPropagation(); openPopup(this)">Detalhes</button>
                            </td>
 */
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.prosaude.controllers.requests.SimulacaoRequest;
import com.prosaude.dto.SimulacaoDTO;
import com.prosaude.model.Simulacao;
import com.prosaude.services.SimulacaoService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/simulacao")
@RequiredArgsConstructor
public class SimulacaoController {

  private final SimulacaoService simulacaoService;
  private final ModelMapper modelMapper;

    @GetMapping
    public String simulacaoForm(Model model) {
        model.addAttribute("simulacao", new SimulacaoRequest());
        return "simulacao/form";
    }

    @PostMapping
    public String salvarSimulacao(@ModelAttribute SimulacaoRequest simulacaoRequest, Model model) {
        Simulacao simulacao = simulacaoService.processarSimulacao(simulacaoRequest);
        SimulacaoDTO simulacaoDTO = modelMapper.map(simulacao, SimulacaoDTO.class);

        // Adicionar os valores calculados ao modelo para exibição
        model.addAttribute("mensagem", String.format(
            "Você pagou R$ %.2f e o valor devido seria R$ %.2f. Uma diferença de R$ %.2f",
            simulacao.getTotalPago(),
            simulacao.getTotalDevido(),
            simulacao.getTotalPago().subtract(simulacao.getTotalDevido())
        ));
        model.addAttribute("simulacao", simulacaoDTO);
        return "simulacao/resultado";
    }
    
    
    @GetMapping("/admin/listar")
    public String listarSimulacoes(Model model) {
        List<Simulacao> simulacoes = simulacaoService.obterSimulacoes();
        List<SimulacaoDTO> simulacoesDTO = simulacoes.stream()
            .map(simulacao -> modelMapper.map(simulacao, SimulacaoDTO.class))
            .toList();
        model.addAttribute("simulacoes", simulacoesDTO);
        return "simulacao/listar";
    }
    
    @GetMapping("/detalhes/{id}")
    @ResponseBody
    public SimulacaoDTO obterDetalhesSimulacao(@PathVariable Long id) {
        Simulacao simulacao = simulacaoService.obterSimulacaoPorId(id);
        return modelMapper.map(simulacao, SimulacaoDTO.class);
    }
}