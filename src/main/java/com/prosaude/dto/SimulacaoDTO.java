package com.prosaude.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoDTO implements Serializable {

  private static final long serialVersionUID = 5313390546489815581L;
  
  private Long id;
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime dataSimulacao;
  private ClienteDTO cliente;
  private List<ItemSimulacaoDTO> itens;
  private BigDecimal valorTotalPago;
  private BigDecimal valorTotalANS;
  private BigDecimal percentualDiferenca;
}
