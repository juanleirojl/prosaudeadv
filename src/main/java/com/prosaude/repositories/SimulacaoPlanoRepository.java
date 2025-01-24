package com.prosaude.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.prosaude.model.ItemSimulacao;

@Repository
public interface SimulacaoPlanoRepository extends JpaRepository<ItemSimulacao, Long> {}
