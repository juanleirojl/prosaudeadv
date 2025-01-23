package com.prosaude.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
  private LocalDate dataSimulacao;
  private ClienteDTO cliente;
  private List<SimulacaoPlanoDTO> simulacoesPlano;
  private BigDecimal totalPago;
  private BigDecimal totalDevido;
  private BigDecimal percentualDiferenca;
}
