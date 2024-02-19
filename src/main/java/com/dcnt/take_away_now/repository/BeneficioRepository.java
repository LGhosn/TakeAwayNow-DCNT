package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.Beneficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {

    Optional<Beneficio> findByNombre(String nombre);

}
