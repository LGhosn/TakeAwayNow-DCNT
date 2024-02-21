package com.dcnt.take_away_now;

import com.dcnt.take_away_now.domain.Cliente;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ClienteTests {

    @Test
    void contextLoads() {
    }

    @Test
    void cumpleOkEnAnioBisiesto() {
        // given: "dado un cliente mayor de edad"
        Cliente cliente = new Cliente("Messi");
        cliente.establecerFechaDeNacimiento(
                LocalDate.of(1996, 2, 29),
                LocalDate.of(2024, 2, 29)
        );

        // then: "vemos si es su cumpleaños"
        assertThat(
                cliente.esSuCumpleanios(
                        LocalDate.of(2024, 2, 29)
                )
        ).isTrue();
    }

}
