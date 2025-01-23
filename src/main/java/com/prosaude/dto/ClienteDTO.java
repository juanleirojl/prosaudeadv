package com.prosaude.dto;

import java.io.Serializable;
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
public class ClienteDTO implements Serializable {
  
  private static final long serialVersionUID = 7925874234637858369L;
  private Long id;
  private String nome;
  private String telefone;
  private String email;
  private LocalDate dataNascimento;
  private List<SimulacaoPlanoDTO> simulacoes;

  
}