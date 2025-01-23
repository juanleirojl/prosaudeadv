package com.prosaude.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndiceANSDTO implements Serializable {
  
  private static final long serialVersionUID = 6442465244125516044L;
  private Long id;
  private BigDecimal percentual;
  private LocalDate dataInicio;
  private LocalDate dataFim;

}
