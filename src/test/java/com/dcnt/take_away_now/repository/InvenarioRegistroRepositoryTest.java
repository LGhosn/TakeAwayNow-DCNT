package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.InventarioRegistro;
import com.dcnt.take_away_now.domain.Negocio;
import com.dcnt.take_away_now.domain.Producto;
import com.dcnt.take_away_now.dto.InventarioRegistroDto;
import com.dcnt.take_away_now.dto.ProductoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class InvenarioRegistroRepositoryTest {
    @Autowired
    private InventarioRegistroRepository inventarioRegistroRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private NegocioRepository negocioRepository;
    Negocio negocio1;
    Negocio negocio2;
    Producto producto1;
    Producto producto2;
    InventarioRegistro inventarioRegistro;

    @BeforeEach
    void setUp() {
        // crea negocios
        negocio1 = new Negocio("Negocio 1", LocalTime.of(14, 0),LocalTime.of(21, 0), DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        negocioRepository.save(negocio1);
        negocio2 = new Negocio("Negocio 2", LocalTime.of(14, 0),LocalTime.of(21, 0), DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        negocioRepository.save(negocio2);
        // crea productos
        producto1 = new Producto("Producto 1");
        productoRepository.save(producto1);
        producto2 = new Producto("Producto 2");
        productoRepository.save(producto2);
        // crea un inventario registro
        inventarioRegistro = new InventarioRegistro();
        inventarioRegistroRepository.save(inventarioRegistro);
    }

    @Test
    void seEncuentraInventarioRegistroPorNegocioYProducto() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        //when: se busca por el negocio y el producto
        boolean encontrado = inventarioRegistroRepository.findByNegocioAndProducto(negocio1, producto1).isPresent();
        //then: se lo encuentra
        assertThat(encontrado).isTrue();
    }

    @Test
    void noSeEncuentraInventarioRegisrtroPorNegocioYProductoInexistente() {
        //given: un inventario registro sin un negocio y un producto
        //when: se busca por el negocio y el producto
        boolean encontrado = inventarioRegistroRepository.findByNegocioAndProducto(negocio1, producto1).isPresent();
        //then: no se lo encuentra
        assertThat(encontrado).isFalse();
    }

    @Test
    void seEliminaInventarioRegistroPorNegocioYProducto() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        //when: se borra por el negocio y el producto
        inventarioRegistroRepository.deleteByNegocioAndProducto(negocio1, producto1);
        //then: fue borrado
        assertThat(inventarioRegistroRepository.findByNegocioAndProducto(negocio1, producto1).isPresent()).isFalse();
    }

    @Test
    void noSeEliminaInventarioRegistroPorNegocioYProductoConProductoIncorrecto() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        //when: se borra por el negocio correcto y el producto incorrecto
        inventarioRegistroRepository.deleteByNegocioAndProducto(negocio1, producto2);
        //then: no fue borrado
        assertThat(inventarioRegistroRepository.findByNegocioAndProducto(negocio1, producto1).isPresent()).isTrue();
    }

    @Test
    void noSeEliminaInventarioRegistroPorNegocioYProductoConNegocioIncorrecto() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        //when: se borra por el negocio incorrecto y el producto correcto
        inventarioRegistroRepository.deleteByNegocioAndProducto(negocio2, producto1);
        //then: no fue borrado
        assertThat(inventarioRegistroRepository.findByNegocioAndProducto(negocio1, producto1).isPresent()).isTrue();
    }

    @Test
    void noSeEliminaInventarioRegistroPorNegocioYProductoConNegocioYProductoIncorrectos() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        //when: se borra por el negocio incorrecto y el producto incorrecto
        inventarioRegistroRepository.deleteByNegocioAndProducto(negocio2, producto2);
        //then: no fue borrado
        assertThat(inventarioRegistroRepository.findByNegocioAndProducto(negocio1, producto1).isPresent()).isTrue();
    }

    @Test
    void existeInventarioRegistroPorNegocioYProducto() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        //when: se busca por el negocio y el producto
        boolean encontrado = inventarioRegistroRepository.existsByNegocioAndProducto(negocio1, producto1);
        //then: se lo encuentra
        assertThat(encontrado).isTrue();
    }

    @Test
    void noExisteInventarioRegistroPorNegocioYProductoInexistente() {
        //given: un inventario registro sin un negocio y un producto
        //when: se busca por el negocio y el producto
        boolean encontrado = inventarioRegistroRepository.existsByNegocioAndProducto(negocio1, producto1);
        //then: no se lo encuentra
        assertThat(encontrado).isFalse();
    }

    @Test
    void noExisteInventarioRegistroPorNegocioyProductoConNegocioIncorrecto() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        //when: se busca por el negocio incorrecto y el producto correcto
        boolean encontrado = inventarioRegistroRepository.existsByNegocioAndProducto(negocio2, producto1);
        //then: no se lo encuentra
        assertThat(encontrado).isFalse();
    }

    @Test
    void noExisteInventarioRegistroPorNegocioYProductoConProductoIncorrecto() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        //when: se busca por el negocio correcto y el producto incorrecto
        boolean encontrado = inventarioRegistroRepository.existsByNegocioAndProducto(negocio1, producto2);
        //then: no se lo encuentra
        assertThat(encontrado).isFalse();
    }

    @Test
    void noExisteInventarioRegistroPorNegocioYProductoConNegocioYProductoIncorrecto() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        //when: se busca por el negocio incorrecto y el producto incorrecto
        boolean encontrado = inventarioRegistroRepository.existsByNegocioAndProducto(negocio2, producto2);
        //then: no se lo encuentra
        assertThat(encontrado).isFalse();
    }

    @Test
    void noSeEncuentranProductosDelNegocioSinProductos() {
        //given: un inventario registro sin un negocio y sin productos
        //when: se buscan productos del negocio
        Collection<ProductoDto> productoDtos = inventarioRegistroRepository.obtenerProductosDelNegocio(negocio1.getId());
        //then: no se obtuvo ningun producto
        assertThat(productoDtos.size()).isEqualTo(0);
    }

    @Test
    void seEncuentraElProductoDelNegocioConIdNegocio() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        //when: se buscan productos del negocio con id negocio
        Collection<ProductoDto> productoDtos = inventarioRegistroRepository.obtenerProductosDelNegocio(negocio1.getId());
        //then: se obtuvo un producto
        assertThat(productoDtos.size()).isEqualTo(1);
        // and: el producto es el correcto
        assertThat(productoDtos.stream().findFirst().get().getNombre()).isEqualTo("Producto 1");
    }

    @Test
    void seEncuentranLosProductosDelNegocioConIdNegocio() {
        //given: un inventario registro con un negocio y un producto
        inventarioRegistro.setProducto(producto1);
        inventarioRegistro.setNegocio(negocio1);
        // and: otro inventario registro con negocio y un producto
        InventarioRegistro inventarioRegistro2 = new InventarioRegistro();
        inventarioRegistro2.setProducto(producto2);
        inventarioRegistro2.setNegocio(negocio1);
        inventarioRegistroRepository.save(inventarioRegistro2);
        //when: se buscan productos del negocio con id negocio
        Collection<ProductoDto> productoDtos = inventarioRegistroRepository.obtenerProductosDelNegocio(negocio1.getId());
        //then: se obtuvieron dos productos
        assertThat(productoDtos.size()).isEqualTo(2);
        // and: los productos son los correctos
        assertThat(productoDtos.stream().findFirst().get().getNombre()).isEqualTo("Producto 1");
        assertThat(productoDtos.stream().skip(1).findFirst().get().getNombre()).isEqualTo("Producto 2");
    }
}
