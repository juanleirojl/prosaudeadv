package com.prosaude.repositories;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.prosaude.model.IndiceANS;

@Repository
public interface IndiceANSRepository extends JpaRepository<IndiceANS, Long> {
  
  @Query("SELECT i FROM IndiceANS i WHERE i.dataInicio <= :data AND i.dataFim > :data")
    Optional<IndiceANS> findIndicePorData(@Param("data") LocalDate data);
}