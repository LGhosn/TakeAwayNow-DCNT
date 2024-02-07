package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NegocioRepository extends JpaRepository<Negocio, Long> {
    Optional<Negocio> findByNombre(String nombre);
}
