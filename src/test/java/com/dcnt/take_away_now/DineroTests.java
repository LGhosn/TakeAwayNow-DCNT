package com.dcnt.take_away_now;

import com.dcnt.take_away_now.value_object.Dinero;
import org.junit.jupiter.api.Test;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class DineroTests {

    @Test
    void contextLoads() {
    }

    @Test
    void tengoDosPesosLeSumoTresPesosYObtengoCincoPesos() {
        // given: "dado dos pesos y tres pesos"
        Dinero dosPesos = new Dinero(2);
        Dinero tresPesos = new Dinero(3);

        // when: "al sumarlos"
        Dinero cincoPesos = dosPesos.plus(tresPesos);

        // then: "obtengo 5 pesos"
        assertThat(cincoPesos.getMonto()).isEqualTo(new BigDecimal(5));
    }

    @Test
    void tengoDosPesosLoMultiplicoPorCincoYObtengoDiezPesos() {
        // given: "dos pesos"
        Dinero dosPesos = new Dinero(2);

        // when: "lo multiplico por cinco"
        Dinero diezPesos = dosPesos.multiply(5);

        // then: "obtengo diez pesos"
        assertThat(diezPesos.getMonto()).isEqualTo(new BigDecimal(10));
    }

    @Test
    void noPuedoMultiplicarDineroPorCero() {
        // given: "dos pesos"
        Dinero dosPesos = new Dinero(2);

        // when: "lo multiplico por cero"
        assertThatThrownBy(() -> dosPesos.multiply(0))

        // then: "obtengo cero pesos"
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No se puede multiplicar el dinero por cero o un numero negativo.");
    }

    @Test
    void noPuedoMultiplicarDineroPorUnNumeroNegativo() {
        // given: "dos pesos"
        Dinero dosPesos = new Dinero(2);

        // when: "lo multiplico por menos uno"
        assertThatThrownBy(() -> dosPesos.multiply(-1))

        // then: "obtengo menos dos pesos"
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No se puede multiplicar el dinero por cero o un numero negativo.");
    }

    @Test
    void noPuedoTenerDineroNegativo() {
        // given: "dado dos pesos y tres pesos"
        Dinero dosPesos = new Dinero(2);
        Dinero tresPesos = new Dinero(3);

        // when: "al restarlos"
        Dinero montoNegativo = dosPesos.minus(tresPesos);

        // then: "lanza excepción"
        assertThat(montoNegativo.getMonto()).isEqualTo(new BigDecimal(-1));
    }

    @Test
    void conviertoDosPesosEnDineroABigDecimal() {
        // given: "dado dos pesos"
        Dinero dosPesos = new Dinero(2);
        DineroAttributeConverter attributeConverter = new DineroAttributeConverter();

        // when: "al convertirlo a columna de la bd"
        BigDecimal dosPesosMonto = attributeConverter.convertToDatabaseColumn(dosPesos);

        // then: "obtengo un big decimal con val = 2"
        assertThat(dosPesosMonto).isEqualTo(new BigDecimal(2));
    }

    @Test
    void conviertoVariableBigDecimalConValIgualADosAValueObjectDineroConMontoIgualADos() {
        // given: "dado una variable BigDecimal con val igual a dos"
        BigDecimal val = new BigDecimal(2);
        DineroAttributeConverter attributeConverter = new DineroAttributeConverter();

        // when: "al convertirlo a un value object"
        Dinero dosPesos = attributeConverter.convertToEntityAttribute(val);

        // then: "obtengo un big decimal con val = 2"
        assertThat(dosPesos).isEqualTo(new Dinero(2));
    }

    @Test
    void tengoDosPesosLoDividoPorDosYObtengoUnPeso() {
        // given: "dos pesos"
        Dinero dosPesos = new Dinero(2);

        // when: "lo divido por dos"
        Dinero unPeso = dosPesos.divide(2);

        // then: "obtengo un peso"
        assertThat(unPeso.getMonto()).isEqualTo(new BigDecimal(1));
    }

    @Test
    void noPuedoDividirDineroPorCero() {
        // given: "dos pesos"
        Dinero dosPesos = new Dinero(2);

        // when: "lo divido por cero"
        assertThatThrownBy(() -> dosPesos.divide(0))

        // then: "obtengo cero pesos"
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No se puede dividir el dinero por cero o un numero negativo.");
    }
}
