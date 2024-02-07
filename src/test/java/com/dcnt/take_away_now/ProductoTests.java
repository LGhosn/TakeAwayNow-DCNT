package com.dcnt.take_away_now;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
// Los tests se encuentran momentaneamente comentados ya que lo referido al stock, pdc y precio fue movido a la tabla de correlacion INVENTARIO_REGISTROS
class ProductoTests {

    @Test
    void contextLoads() {
    }

    /*@Test
    void noPuedoCrearProductosConPrecioMenorACero() {
        // when: "al crear un producto con cantidad menor a cero"
        assertThatThrownBy(() -> new Producto("Alfajor", -10, 100))

        // then: "lanzo una excepción"
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Un producto no puede ser creado con un precio menor o igual a cero.");
    }

    @Test
    void noPuedoCrearProductosConPrecioIgualACero() {
        // when: "al crear un producto con cantidad menor a cero"
        assertThatThrownBy(() -> new Producto("Alfajor", 0, 100))

                // then: "lanzo una excepción"
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Un producto no puede ser creado con un precio menor o igual a cero.");
    }

    @Test
    void noPuedoCrearProductosConUnaCantidadParaLaRecompensaPuntosDeConfianzaMenorACero() {
        // when: "al crear un producto con cantidad menor a cero"
        assertThatThrownBy(() -> new Producto("Alfajor", 100, -100))

                // then: "lanzo una excepción"
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Un producto no puede ser creado con una cantidad negativa de Puntos de Confianza como recompensa.");
    }

    @Test
    void seCreaCorrectamenteUnProductoConCantidadParaLaRecompensaDePuntosDeConfianzaYMontoMayoresACero() {
        // when: "se crea un producto con puntos de confianza y monto mayores a cero"
        Producto alfajor = new Producto("Alfajor", 10, 100);

        // then: "sus puntos de confianza son correctos"
        assertThat(alfajor.getRecompensaPuntosDeConfianza().getClass()).isEqualTo(PuntosDeConfianza.class);
        assertThat(alfajor.getRecompensaPuntosDeConfianza().getCantidad()).isEqualTo(100);

        // and: "su precio es correcto"
        assertThat(alfajor.getPrecio().getClass()).isEqualTo(Dinero.class);
        assertThat(alfajor.getPrecio().getMonto()).isEqualTo(new BigDecimal(10));
    }*/
}
