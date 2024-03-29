package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.Cliente;
import com.dcnt.take_away_now.domain.Plan;
import com.dcnt.take_away_now.repository.ClienteRepository;
import com.dcnt.take_away_now.repository.PedidoRepository;
import com.dcnt.take_away_now.repository.PlanRepository;
import com.dcnt.take_away_now.repository.ProductoPedidoRepository;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.http.HttpStatus.*;

@DataJpaTest
class ClienteServiceTest {
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private ProductoPedidoRepository productoPedidoRepository;
    @Autowired
    private PlanRepository planRepository;

    private ClienteService clienteService;

    private String username;
    @BeforeEach
    void setUp() {
        username = "Lionel Messi";
        clienteService = new ClienteService(clienteRepository, pedidoRepository, productoPedidoRepository, planRepository);
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
        // given
        clienteService.crearCliente(username);

        // when
        assertThatThrownBy(() -> clienteService.crearCliente(username))

        // then: "obtengo cero pesos"
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
        // when
        assertThatThrownBy(() -> clienteService.cargarSaldo(1L, BigDecimal.valueOf(100)))

        // then: "obtengo cero pesos"
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No existe el cliente en la base de datos.");
    }

    @Test
    void alCrearUnClienteNoEsPrime() {
        // given
        Cliente cliente = new Cliente(username);

        // when
        boolean esPrime = cliente.esPrime();

        // then
        assertThat(esPrime).isFalse();
    }

    // TESTS NO PUEDO OBTENER PLAN PRIME SI NO EXISTE
    @Test
    void noPuedoObtenerPlanPrimeSiNoExiste() {
        // given
        new Cliente(username);
        clienteService.crearCliente(username);
        Optional<Cliente> messi = clienteRepository.findByUsuario(username);

        if (messi.isEmpty()) {
            throw new AssertionError("No se encontró el cliente creado.");
        }

        // when
        assertThatThrownBy(() -> clienteService.obtenerPlanPrime(messi.get().getId()))

        // then: "obtengo cero pesos"
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No existe el plan Prime en la base de datos.");
    }

    @Test
    void noPuedoObtenerPlanPrimeSiNoExisteElCliente() {
        // when
        assertThatThrownBy(() -> clienteService.obtenerPlanPrime(0L))

        // then: "obtengo cero pesos"
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No existe el cliente en la base de datos.");
    }

    @Test
    void noPuedoObtenerPlanPrimeConSaldoInsuficiente() {
        // given
        new Cliente(username);
        clienteService.crearCliente(username);
        Optional<Cliente> messi = clienteRepository.findByUsuario(username);

        // si no existe plan prime crearlo
        Optional<Plan> optionalPlanPrime = planRepository.findByNombre("Prime");
        if (optionalPlanPrime.isEmpty()) {
            Plan planPrime = new Plan(
                    "Prime",
                    new Dinero(100),
                    new PuntosDeConfianza(100),
                    10,
                    2,
                    true,
                    5
            );

            planRepository.save(planPrime);
        }

        // when
        if (messi.isEmpty()) {
            throw new AssertionError("No se encontró el cliente creado.");
        }

        // when
        assertThatThrownBy(() -> clienteService.obtenerPlanPrime(messi.get().getId()))

        // then: "obtengo cero pesos"
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No posees saldo suficiente para adquirir el plan Prime.");
    }

    @Test
    void unClienteNoPuedeVolverASubscribirseAlPlanPrime() {
        // given
        new Cliente(username);
        clienteService.crearCliente(username);
        Optional<Cliente> messi = clienteRepository.findByUsuario(username);

        // si no existe plan prime crearlo
        Optional<Plan> optionalPlanPrime = planRepository.findByNombre("Prime");
        if (optionalPlanPrime.isEmpty()) {
            Plan planPrime = new Plan(
                    "Prime",
                    new Dinero(100),
                    new PuntosDeConfianza(100),
                    10,
                    2,
                    true,
                    5
            );

            planRepository.save(planPrime);
        }

        // when
        if (messi.isEmpty()) {
            throw new AssertionError("No se encontró el cliente creado.");
        }

        clienteService.cargarSaldo(messi.get().getId(), BigDecimal.valueOf(200));
        clienteService.obtenerPlanPrime(messi.get().getId());
        // when
        assertThatThrownBy(() -> clienteService.obtenerPlanPrime(messi.get().getId()))

        // then: "obtengo cero pesos"
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Ya estás suscripto al plan Prime.");
        assertThat(messi.get().esPrime()).isTrue();
    }

    @Test
    void alSubscribirseAUnPlanPrimeElClienteEsPrime() {
        // given
        new Cliente(username);
        clienteService.crearCliente(username);
        Optional<Cliente> messi = clienteRepository.findByUsuario(username);

        // si no existe plan prime crearlo
        Optional<Plan> optionalPlanPrime = planRepository.findByNombre("Prime");
        if (optionalPlanPrime.isEmpty()) {
            Plan planPrime = new Plan(
                    "Prime",
                    new Dinero(100),
                    new PuntosDeConfianza(100),
                    10,
                    2,
                    true,
                    5
            );

            planRepository.save(planPrime);
        }

        // when
        if (messi.isEmpty()) {
            throw new AssertionError("No se encontró el cliente creado.");
        }

        clienteService.cargarSaldo(messi.get().getId(), BigDecimal.valueOf(200));
        clienteService.obtenerPlanPrime(messi.get().getId());

        // then
        assertThat(messi.get().esPrime()).isTrue();
    }

    @Test
    void sePuedeEstablercerFechaDeNacimiento() {
        // given
        new Cliente(username);
        clienteService.crearCliente(username);
        Optional<Cliente> messi = clienteRepository.findByUsuario(username);
        int year = 1987;
        int mm = 06;
        int dd = 24;

        // when
        clienteService.establecerFechaDeNacimiento(messi.get().getId(), year, mm, dd);

        // then
        LocalDate fechaDeNacimiento = LocalDate.of(year, mm, dd);
        assertThat(messi.get().getFechaDeNacimiento()).isEqualTo(fechaDeNacimiento);
    }
    @Test
    void noSePuedeEstablercerFechaDeNacimientoSiendoMenorDeEdad() {
        // given
        new Cliente(username);
        clienteService.crearCliente(username);
        Optional<Cliente> messi = clienteRepository.findByUsuario(username);
        int year = 2010;
        int mm = 06;
        int dd = 24;

        // when
        assertThatThrownBy(() -> clienteService.establecerFechaDeNacimiento(messi.get().getId(), year, mm, dd))

        // then: "obtengo cero pesos"
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Debes ser mayor de edad para acceder al beneficio por cumpleaños.");
    }
}
