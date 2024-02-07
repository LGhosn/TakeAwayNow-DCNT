package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class ClienteRepositoryTest {
    @Autowired
    private ClienteRepository clienteRepository;
    @Test
    void sePuedeEncontrarUnUsuarioQueExiste() {
        //given
        Cliente cliente = new Cliente("Lionel Messi");
        clienteRepository.save(cliente);

        //when
        boolean found =  clienteRepository.findByUsuario("Lionel Messi").isPresent();

        //then
        assertThat(found).isTrue();
    }

    @Test
    void noSePuedeEncontrarUnUsuarioQueNoExiste() {
        //when
        boolean found =  clienteRepository.findByUsuario("Lionel Messi").isPresent();

        //then
        assertThat(found).isFalse();
    }
}