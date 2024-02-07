package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.Cliente;
import com.dcnt.take_away_now.domain.Pedido;
import com.dcnt.take_away_now.repository.ClienteRepository;
import com.dcnt.take_away_now.repository.PedidoRepository;
import com.dcnt.take_away_now.repository.ProductoPedidoRepository;
import com.dcnt.take_away_now.value_object.Dinero;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ClienteServiceTest {
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private ProductoPedidoRepository productoPedidoRepository;

    private ClienteService clienteService;

    private String username;
    @BeforeEach
    void setUp() {
        username = "Lionel Messi";
        clienteService = new ClienteService(clienteRepository, pedidoRepository, productoPedidoRepository);
    }

    @Test
    void sePuedeCrearClienteNuevo() {
        //when
        clienteService.crearCliente(username);

        //then
        Optional<Cliente> cliente = clienteRepository.findByUsuario(username);
        assertThat(cliente.get().getUsuario()).isEqualTo(username);
    }

    @Test
    void noSePuedeCrearDosClientesConElMismoUsername() {
        //given
        clienteService.crearCliente(username);
        //when
        assertThatThrownBy(
                () -> {
                    clienteService.crearCliente(username);
                }
        )
        // then: "se lanza error"
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Ya existe un usuario con el nombre ingresado.");
    }

    @Test
    void sePuedeObtenerClientesExistentes() {
        //given
        clienteService.crearCliente(username);
        clienteService.crearCliente(username + " 10");
        clienteService.crearCliente(username + " Campeon");

        //when
        Collection<Cliente> clientes = clienteService.obtenerClientes();

        assertThat(clientes.size()).isEqualTo(3);
        assertThat(existeUsuario(clientes, username)).isTrue();
        assertThat(existeUsuario(clientes, username + " 10")).isTrue();
        assertThat(existeUsuario(clientes, username + " Campeon")).isTrue();

    }

    private boolean existeUsuario(Collection<Cliente> clientes, String username) {
        return clientes.stream()
                .anyMatch(cliente -> cliente.getUsuario().equals(username));
    }

    @Test
    void sePuedeEliminarUnCliente() {
        //given
        clienteService.crearCliente(username);
        Optional<Cliente> messi = clienteRepository.findByUsuario(username);

        //when
        clienteService.eliminarCliente(messi.get().getId());

        //then
        boolean found = clienteRepository.findByUsuario(username).isPresent();
        assertThat(found).isFalse();
    }

    @Test
    void sePuedeCargarSaldoAUnCliente() {
        //given
        clienteService.crearCliente(username);
        Optional<Cliente> messi = clienteRepository.findByUsuario(username);

        //when
        clienteService.cargarSaldo(messi.get().getId(), BigDecimal.valueOf(100));

        //then
        Dinero saldo = messi.get().getSaldo();
        assertThat(saldo).isEqualTo(new Dinero(100));
    }

    @Test
    void noSePuedeCargarSaldoAUnClienteQueNoExiste() {
        //when
        assertThatThrownBy(
                () -> {
                    clienteService.cargarSaldo(1L, BigDecimal.valueOf(100));

                }
        )
                // then: "se lanza error"
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No existe el cliente en la base de datos.");
    }
}
