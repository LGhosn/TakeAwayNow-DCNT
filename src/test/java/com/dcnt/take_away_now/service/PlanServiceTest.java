package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.Cliente;
import com.dcnt.take_away_now.domain.Plan;
import com.dcnt.take_away_now.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import java.util.Collection;
import java.util.Optional;

@DataJpaTest
class PlanServiceTest {
    @Autowired
    private PlanRepository planRepository;

    private PlanService planService;

    private String nombre;
    private int precio;
    private int puntosDeConfianza;
    private int descuento;
    private int multiplicadorDePuntosDeConfianza;
    private boolean cancelacionSinCosto;
    private int porcentajeExtraDePuntosDeConfianzaPorDevolucion;

    //Set up
    @BeforeEach
    void setUp() {
        nombre = "Plan";
        precio = 300;
        puntosDeConfianza = 100;
        descuento = 10;
        multiplicadorDePuntosDeConfianza = 2;
        cancelacionSinCosto = true;
        porcentajeExtraDePuntosDeConfianzaPorDevolucion = 5;

        planService = new PlanService(planRepository);
    }

    @Test
    void sePuedeCrearPlanNuevo() {
        // given
        planService.crearPlan(
            nombre,
            precio,
            puntosDeConfianza,
            descuento,
            multiplicadorDePuntosDeConfianza,
            cancelacionSinCosto,
            porcentajeExtraDePuntosDeConfianzaPorDevolucion
        );

        // when
        Optional<Plan> plan = planRepository.findByNombre(nombre);

        // then
        if (plan.isEmpty()) {
            throw new AssertionError("No se encontró el plan creado.");
        }

        assertThat(plan.get().getNombre()).isEqualTo(nombre);
    }

    @Test
    void noSePuedeCrearDosPlanesConElMismoNombre() {
        // given
        planService.crearPlan(
            nombre,
            precio,
            puntosDeConfianza,
            descuento,
            multiplicadorDePuntosDeConfianza,
            cancelacionSinCosto,
            porcentajeExtraDePuntosDeConfianzaPorDevolucion
        );
        assertThatThrownBy(() -> {
            planService.crearPlan(
                    nombre,
                    precio,
                    puntosDeConfianza,
                    descuento,
                    multiplicadorDePuntosDeConfianza,
                    cancelacionSinCosto,
                    porcentajeExtraDePuntosDeConfianzaPorDevolucion
            );
        })
        // then: "obtengo cero pesos"
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Ya existe un plan con el nombre ingresado.");
    }

    @Test
    void sePuedenEncontrarUnPlanesQueExistes() {
        // given
        planService.crearPlan(
                nombre,
                precio,
                puntosDeConfianza,
                descuento,
                multiplicadorDePuntosDeConfianza,
                cancelacionSinCosto,
                porcentajeExtraDePuntosDeConfianzaPorDevolucion
        );

        planService.crearPlan(
                nombre + "2",
                precio,
                puntosDeConfianza,
                descuento,
                multiplicadorDePuntosDeConfianza,
                cancelacionSinCosto,
                porcentajeExtraDePuntosDeConfianzaPorDevolucion
        );

        // when
        Collection<Plan> planes = planService.obtenerPlanes();

        // then
        assertThat(existePlan(planes, nombre)).isTrue();
    }

    @Test
    void noSePuedeEncontrarUnPlanQueNoExiste() {
        // when
        Collection<Plan> planes = planService.obtenerPlanes();

        // then
        assertThat(existePlan(planes, nombre)).isFalse();
    }

    @Test
    void sePuedeEliminarUnPlanExistente() {
        // given
        planService.crearPlan(
                nombre,
                precio,
                puntosDeConfianza,
                descuento,
                multiplicadorDePuntosDeConfianza,
                cancelacionSinCosto,
                porcentajeExtraDePuntosDeConfianzaPorDevolucion
        );

        // when
        Optional<Plan> plan = planRepository.findByNombre(nombre);

        if (plan.isEmpty()) {
            throw new AssertionError("No se encontró el plan creado.");
        }

        planService.eliminarPlan(plan.get().getId());

        // then
        assertThat(planRepository.findByNombre(nombre).isEmpty()).isTrue();
    }

    boolean existePlan(Collection<Plan> planes, String nombre) {
        return planes.stream()
                .anyMatch(cliente -> cliente.getNombre().equals(nombre));
    }
}
