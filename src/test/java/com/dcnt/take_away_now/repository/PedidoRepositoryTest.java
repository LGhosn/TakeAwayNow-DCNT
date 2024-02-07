package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.Cliente;
import com.dcnt.take_away_now.domain.Negocio;
import com.dcnt.take_away_now.domain.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class PedidoRepositoryTest {
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    NegocioRepository negocioRepository;

    Cliente cliente;

    Negocio negocio;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("Lionel Messi");
        clienteRepository.save(cliente);
        negocio = new Negocio("Paseo Colon", LocalTime.of(14, 0),LocalTime.of(21, 0), DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        negocioRepository.save(negocio);
    }

    @Test
    void seEncuentraUnPedidoQueExisteById() {
        //given
        Pedido pedido = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido);

        //when
        assertThat(pedidoRepository.existsPedidoById(pedido.getId())).isTrue();
    }

    @Test
    void noSeEncuentraUnPedidoQueNoExisteById() {
        //when
        assertThat(pedidoRepository.existsPedidoById(1L)).isFalse();
    }
}