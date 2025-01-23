package com.prosaude.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.prosaude.model.Simulacao;

@Repository
public interface SimulacaoRepository extends JpaRepository<Simulacao, Long> {}
