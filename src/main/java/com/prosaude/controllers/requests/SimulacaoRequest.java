package com.prosaude.controllers.requests;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import com.prosaude.model.TipoAumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimulacaoRequest {
  private String nome;
  private String email;
  private String telefone;
  private LocalDate dataNascimento;
  
  @Builder.Default
  private List<ItemSimulacao> itens = new ArrayList<>();

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ItemSimulacao {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;
    private BigDecimal valor;
    private TipoAumento tipoAumento;
  }
}
