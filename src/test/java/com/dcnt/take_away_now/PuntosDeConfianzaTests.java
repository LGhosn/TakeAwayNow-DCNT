package com.dcnt.take_away_now;

import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import com.dcnt.take_away_now.value_object.converter.PuntosDeConfianzaAttributeConverter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PuntosDeConfianzaTests {

    @Test
    void contextLoads() {
    }

    @Test
    void sumarPuntosConCantidad100YPuntosConCantidad50ElResultadoEs150() {
        // given: "dos puntos de confianza con cantidad 100 y 50"
        PuntosDeConfianza puntos100 = new PuntosDeConfianza(100);
        PuntosDeConfianza puntos50 = new PuntosDeConfianza(50);

        // when: "al sumarlos"
        PuntosDeConfianza puntosResultantes = puntos100.plus(puntos50);

        // then: "la suma da puntos con cantidad 150"
        assertThat(puntosResultantes.getCantidad()).isEqualTo(150);
    }

    @Test
    void restarPuntosConCantidad100YPuntosConCantidad50ElResultadoEs50() {
        // given: "dos puntos de confianza con cantidad 100 y 50"
        PuntosDeConfianza puntos100 = new PuntosDeConfianza(100);
        PuntosDeConfianza puntos50 = new PuntosDeConfianza(50);

        // when: "al restarlos"
        PuntosDeConfianza puntosResultantes = puntos100.minus(puntos50);

        // then: "la resta da puntos con cantidad 50"
        assertThat(puntosResultantes.getCantidad()).isEqualTo(50);
    }

    @Test
    void sumar50APuntosConCantidad100ElResultadoEs150() {
        // given: "puntos de confianza con cantidad 100"
        PuntosDeConfianza puntos100 = new PuntosDeConfianza(100);

        // when: "al sumarle 50"
        PuntosDeConfianza puntosResultantes = puntos100.plus(50);

        // then: "la suma da puntos con cantidad 150"
        assertThat(puntosResultantes.getCantidad()).isEqualTo(150);
    }

    @Test
    void restar150APuntosConCantidad100ElResultadoEsMenos50() {
        // given: "puntos de confianza con cantidad 100"
        PuntosDeConfianza puntos100 = new PuntosDeConfianza(100);

        // when: "al restarle 150"
        PuntosDeConfianza puntosResultantes = puntos100.minus(150);

        // then: "se lanza un error"
        assertThat(puntosResultantes.getCantidad()).isEqualTo(-50);
    }

    @Test
    void alMultiplicarPuntosConCantidad100PorMenosUnoSeLanzaUnError() {
        // given: "puntos de confianza con cantidad 100"
        PuntosDeConfianza puntos100 = new PuntosDeConfianza(100);

        // when: "al multiplicar por -1"
        assertThatThrownBy(
                () -> puntos100.multiply(-1)
        )

        // then: "se lanza un error"
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No se pueden multiplicar puntos de confianza por números menores a cero.");
        // and: "seguimos con los mismos puntos"
        assertThat(puntos100.getCantidad()).isEqualTo(100);
    }

    @Test
    void alMultiplicarPuntosConCantidad100Por5ElResultadoEs500() {
        // given: "puntos de confianza con cantidad 100"
        PuntosDeConfianza puntos100 = new PuntosDeConfianza(100.0);

        // when: "al restarle 150"
        PuntosDeConfianza puntosDeConfianzaResultantes = puntos100.multiply(5.0);

        // then: "se obtiene una instancia de PuntosDeConfianza con cantidad igual a 500"
        assertThat(puntosDeConfianzaResultantes.getCantidad()).isEqualTo(500);
    }

    @Test
    void conviertoDiezPuntosDeConfianzaEnInteger() {
        // given: "dado diez puntos de confianza"
        PuntosDeConfianza diezPuntosDeConfianza = new PuntosDeConfianza(10.0);
        PuntosDeConfianzaAttributeConverter attributeConverter = new PuntosDeConfianzaAttributeConverter();

        // when: "al convertirlo a columna de la bd"
        Double cantidadResultante = attributeConverter.convertToDatabaseColumn(diezPuntosDeConfianza);

        // then: "obtengo un Integer con val = 2"
        assertThat(cantidadResultante).isEqualTo(10);
    }

    @Test
    void conviertoVariableIntegerConValIgualADiezAValueObjectPuntosDeConfianzaConCantidadIgualADiez() {
        // given: "dado una variable Integer con val igual a diez"
        Integer val = 10;
        PuntosDeConfianzaAttributeConverter attributeConverter = new PuntosDeConfianzaAttributeConverter();

        // when: "al convertirlo a un value object"
        PuntosDeConfianza diezPuntosDeConfianza = attributeConverter.convertToEntityAttribute(Double.valueOf(val));

        // then: "obtengo una instancia de PuntosDeConfianza con cantidad igual a 10"
        assertThat(diezPuntosDeConfianza.getClass()).isEqualTo(PuntosDeConfianza.class);
        assertThat(diezPuntosDeConfianza.getCantidad()).isEqualTo(10);
    }
}
