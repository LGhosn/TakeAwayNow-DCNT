package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.Plan;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class PlanRespositoryTest {
    @Autowired
    private PlanRepository planRepository;
    private String nombre;
    private Dinero precio;
    private PuntosDeConfianza puntosDeConfianza;
    private int descuento;
    private int multiplicadorDePuntosDeConfianza;
    private boolean cancelacionSinCosto;
    private int porcentajeExtraDePuntosDeConfianzaPorDevolucion;

    //Set up
    @BeforeEach
    void setUp() {
        nombre = "Plan";
        precio = new Dinero(300);
        puntosDeConfianza = new PuntosDeConfianza(100);
        descuento = 10;
        multiplicadorDePuntosDeConfianza = 2;
        cancelacionSinCosto = true;
        porcentajeExtraDePuntosDeConfianzaPorDevolucion = 5;
    }

    @Test
    void sePuedeEncontrarUnPlanQueExiste() {
        //given
        Plan plan = new Plan(
                nombre,
                precio,
                puntosDeConfianza,
                descuento,
                multiplicadorDePuntosDeConfianza,
                cancelacionSinCosto,
                porcentajeExtraDePuntosDeConfianzaPorDevolucion
        );
        planRepository.save(plan);

        //when
        boolean found =  planRepository.findByNombre(nombre).isPresent();

        //then
        assertThat(found).isTrue();
    }

    @Test
    void noSePuedeEncontrarUnPlanQueNoExiste() {
        //when
        boolean found =  planRepository.findByNombre(nombre + "inexistente").isPresent();

        //then
        assertThat(found).isFalse();
    }

    // LOS valores del plan encontrado son correctos
    @Test
    void valoresDelPlanEncontradoSonCorrectos() {
        //given
        Plan plan = new Plan(
                nombre,
                precio,
                puntosDeConfianza,
                descuento,
                multiplicadorDePuntosDeConfianza,
                cancelacionSinCosto,
                porcentajeExtraDePuntosDeConfianzaPorDevolucion
        );

        planRepository.save(plan);

        //when
        Plan planEncontrado =  planRepository.findByNombre(nombre).get();

        //then
        assertThat(planEncontrado.getNombre()).isEqualTo(nombre);
        assertThat(planEncontrado.getPrecio()).isEqualTo(precio);
        assertThat(planEncontrado.getPuntosDeConfianzaPorSubscripcion()).isEqualTo(puntosDeConfianza);
        assertThat(planEncontrado.getDescuento()).isEqualTo(descuento);
        assertThat(planEncontrado.getMultiplicadorDePuntosDeConfianza()).isEqualTo(multiplicadorDePuntosDeConfianza);
        assertThat(planEncontrado.isCancelacionSinCosto()).isEqualTo(cancelacionSinCosto);
        assertThat(planEncontrado.getPorcentajeExtraDePuntosDeConfianzaPorDevolucion()).isEqualTo(porcentajeExtraDePuntosDeConfianzaPorDevolucion);
    }
}
