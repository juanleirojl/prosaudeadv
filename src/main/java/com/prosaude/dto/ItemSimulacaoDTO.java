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
public class ItemSimulacaoDTO implements Serializable {

  private static final long serialVersionUID = 1276326018516095718L;

  private Long id;

  private BigDecimal valor;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate data;

  private TipoAumento tipoAumento;
  

  private ClienteDTO cliente;
  
  private IndiceANSDTO indiceANS;
  
  private BigDecimal valorAumentoReal;
  private BigDecimal percentualAumentoReal;
  private BigDecimal valorANS;
  private BigDecimal percentualANS;
  private BigDecimal percentualDiferenca;
  
  
}
