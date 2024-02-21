package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.repository.PlanRepository;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.dcnt.take_away_now.domain.Plan;

import java.util.Collection;
import java.util.Optional;

@AllArgsConstructor
@Service
public class PlanService {
    private final PlanRepository planRepository;

    public void crearPlan(
            String nombre,
            int precio,
            int puntosDeConfiazna,
            int descuento,
            int multiplicadorPuntoDeConfianza,
            boolean cancelacionSinCosto,
            int porcentajeDePuntosDeConfianzaPorDevolucion) {
        Optional<Plan> optionalCliente = planRepository.findByNombre(nombre);

        if (optionalCliente.isPresent()) {
            throw new RuntimeException("Ya existe un plan con el nombre ingresado.");
        }

        this.planRepository.save(new Plan(nombre, new Dinero(precio), new PuntosDeConfianza(puntosDeConfiazna), descuento, multiplicadorPuntoDeConfianza, cancelacionSinCosto, porcentajeDePuntosDeConfianzaPorDevolucion));
    }

    public Collection<Plan> obtenerPlanes() {
        return planRepository.findAll();
    }

    public void eliminarPlan(Long planId) {
        Optional<Plan> optionalPlan = planRepository.findById(planId);
        if (optionalPlan.isEmpty()) {
            throw new RuntimeException("No existe el plan en la base de datos.");
        }

        planRepository.deleteById(planId);
    }
}