package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class ProductoRepositoryTest {
    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void SeEncuentraUnProductoQueExiste() {
        //given: un producto llamado Pancho con papas
        Producto producto = new Producto("Pancho con papas");
        productoRepository.save(producto);
        //when: se busca por el producto Pancho con papas
        boolean found = productoRepository.findByNombre("Pancho con papas").isPresent();
        //then: se lo encuentra
        assertThat(found).isTrue();
    }
    @Test
    void NoSeEncuentraUnProductoQueNoExiste() {
        //given: un producto llamado Pancho con papas
        Producto producto = new Producto("Pancho con papas");
        productoRepository.save(producto);
        //when: se busca por el producto Chocolate
        boolean found = productoRepository.findByNombre("Chocolate").isPresent();
        //then: no se lo encuentra
        assertThat(found).isFalse();
    }
}