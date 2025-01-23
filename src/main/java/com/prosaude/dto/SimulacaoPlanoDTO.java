package com.prosaude.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.prosaude.model.TipoAumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoPlanoDTO implements Serializable {

  private static final long serialVersionUID = 1276326018516095718L;

  private Long id;

  private BigDecimal valor;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate data;

  private TipoAumento tipoAumento;
  
  private BigDecimal aumentoPlano;
  private BigDecimal aumentoDevido;
  private BigDecimal percentualAumentoPlano; 
  private BigDecimal percentualDiferenca;

  private IndiceANSDTO indiceANS;
  
  
}
