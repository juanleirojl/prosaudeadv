package com.prosaude.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.prosaude.model.Simulacao;

@Repository
public interface SimulacaoRepository extends JpaRepository<Simulacao, Long> {

  @Query("""
          SELECT s
          FROM Simulacao s
          LEFT JOIN FETCH s.cliente
          LEFT JOIN FETCH s.itens i
          LEFT JOIN FETCH i.cliente
          LEFT JOIN FETCH i.indiceANS
          ORDER BY s.dataSimulacao DESC, i.data ASC
      """)
  List<Simulacao> buscarSimulacoesComItens();

}
