package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.*;
import com.dcnt.take_away_now.dto.InfoPedidoDto;
import com.dcnt.take_away_now.dto.InventarioRegistroDto;
import com.dcnt.take_away_now.dto.PedidoDto;
import com.dcnt.take_away_now.dto.ProductoDto;
import com.dcnt.take_away_now.repository.*;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.ResponseEntity;
import static org.springframework.http.HttpStatus.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class PedidoServiceTest {
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private NegocioRepository negocioRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private InventarioRegistroRepository inventarioRegistroRepository;
    @Autowired
    private ProductoPedidoRepository productoPedidoRepository;
    private NegocioService negocioService;

    private PedidoService pedidoService;

    private ClienteService clienteService;
    private ResponseEntity<String> response;

    Cliente cliente;
    Negocio negocio;
    Producto pancho;
    @BeforeEach
    void setUp() {
        cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        negocio = new Negocio("Paseo Colon", LocalTime.of(14, 0),LocalTime.of(21, 0), DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        negocioRepository.save(negocio);
        pancho = new Producto("Pancho");
        productoRepository.save(pancho);
        clienteService = new ClienteService(clienteRepository, pedidoRepository, productoPedidoRepository);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));
        negocioService = new NegocioService(negocioRepository, inventarioRegistroRepository, productoRepository,pedidoRepository);
        pedidoService = new PedidoService(pedidoRepository, clienteRepository, negocioRepository,productoRepository, inventarioRegistroRepository,productoPedidoRepository);
    }

    @Test
    void obtenerPedidos() {
        //given
        Pedido pedido1 = new Pedido(negocio, cliente );
        pedidoRepository.save(pedido1);
        Pedido pedido2 = new Pedido(negocio, cliente );
        pedidoRepository.save(pedido2);
        Pedido pedido3 = new Pedido(negocio, cliente );
        pedidoRepository.save(pedido3);

        //when
        Collection<Pedido> pedidos = pedidoService.obtenerPedidos();

        //then
        assertThat(pedidos.size()).isEqualTo(3);
    }
    @Test
    void esUnProductoDeEseNegocio() {
        //given
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0), new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        //when
        boolean esProductoDeEseNegocio = pedidoService.esUnProductoDeEseNegocio(negocio.getId(), alfajor.get().getId());

        //then
        assertThat(esProductoDeEseNegocio).isTrue();
    }
    @Test
    void noEsUnProductoDeEseNegocio() {
        //when
        boolean noEsProductoDeEseNegocio = pedidoService.esUnProductoDeEseNegocio(negocio.getId(), pancho.getId());

        //then
        assertThat(noEsProductoDeEseNegocio).isFalse();
    }
    @Test
    void sePuedeConfirmarUnPedidoParaEstosProductos() {
        //given
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        negocioService.crearProducto(negocio.getId(), "Coca Cola",inventarioRegistroDto);
        negocioService.crearProducto(negocio.getId(), "Paraguitas",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");
        Optional<Producto> coca = productoRepository.findByNombre("Coca Cola");
        Optional<Producto> paraguitas = productoRepository.findByNombre("Paraguitas");
        Map<Long, Map<String, Object>> productos =
                Map.of(
                    alfajor.get().getId(), Map.of("cantidad", 2, "usaPdc", 0),
                    coca.get().getId(), Map.of("cantidad", 3, "usaPdc", 0),
                    paraguitas.get().getId(), Map.of("cantidad", 1, "usaPdc", 0)
        );

        //when
        boolean sePuede = pedidoService.sePuedeConfirmarUnPedidoParaEstosProductos(productos, negocio.getId());

        //then
        assertThat(sePuede).isTrue();
    }

    @Test
    void noSePuedeConfirmarUnPedidoParaEstosProductosPorqueNoEsUnProductoDelNegocio() {
        //given
        Map<Long, Map<String, Object>> productos =Map.of(pancho.getId(), Map.of("cantidad", 1, "usaPdc", 0));

        //when
        assertThatThrownBy(
                () -> {
                    pedidoService.sePuedeConfirmarUnPedidoParaEstosProductos(productos, negocio.getId());
                }
        )
        // then: "se lanza error"
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Ha ocurrido un error ya que el producto " + pancho.getNombre() + " no está disponible para este negocio.");
    }
    @Test
    void noSePuedeConfirmarUnPedidoParaEstosProductosPorqueNoHayStockSuficiente() {
        //given
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 11, "usaPdc", 0)
                );

        //when
        assertThatThrownBy(
                () -> {
                    pedidoService.sePuedeConfirmarUnPedidoParaEstosProductos(productos, negocio.getId());
                }
        )
                // then: "se lanza error"
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La cantidad solicitada para el producto " + alfajor.get().getNombre() + " es mayor al stock disponible.");
    }

    @Test
    void sePuedeConfirmarPedido() {
        //given
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 9, "usaPdc", 0)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);

        //when
        response = pedidoService.confirmarPedido(infoPedidoDto);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo("El pedido fue confirmado correctamente.");
    }

    @Test
    void verificarPedido() {
        //given
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 10, "usaPdc", 0)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);

        //when
        response = pedidoService.verificarPedido(infoPedidoDto);

        //then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo("El pedido fue confirmado correctamente.");
    }

    @Test
    void verificarPedidoLanzaQueNoExisteCliente() {
        //given
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 10, "usaPdc", 0)
                );

        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(33L, negocio.getId(), productos);

        //when
        response = pedidoService.verificarPedido(infoPedidoDto);

        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("No existe el cliente para el cual se busca verificar la integridad del pedido.");
    }

    @Test
    void verificarPedidoLanzaQueNoExisteNegocio() {
        //given
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");


        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 10, "usaPdc", 0)
                );

        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), 33L, productos);

        //when
        response = pedidoService.verificarPedido(infoPedidoDto);

        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("No existe el negocio para el cual se busca verificar la integridad del pedido.");
    }

    @Test
    void sePuedeMarcarComienzoDePreparacionAUnPedidoQueEstaAguardandoPreparacion() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);

        //when
        response = pedidoService.marcarComienzoDePreparacion(pedido1.getId());

        //then
        assertThat(response.getStatusCode()).isEqualTo(ACCEPTED);
        assertThat(response.getBody()).isEqualTo("Se ha marcado que el pedido está en comienzo de preparación.");
    }

    @Test
    void noSePuedeMarcarComienzoDePreparacionAUnPedidoQueNoExiste() {
        //when
        response = pedidoService.marcarComienzoDePreparacion(1L);
        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("No existe el pedido al cual usted quiere marcar su comienzo de preparación.");
    }

    @Test
    void noSePuedeMarcarComienzoDePreparacionAUnPedidoQueNoEstaEnAguardandoPreparacion() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);
        pedidoService.marcarComienzoDePreparacion(pedido1.getId());

        //when
        response = pedidoService.marcarComienzoDePreparacion(pedido1.getId());

        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("No se puede comenzar a preparar dicho pedido ya que el mismo no se encuentra aguardando preparación.");
    }

    @Test
    void sePuedeMarcarListoParaRetirarAUnPedidoQueEstaEnPreparacion() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);
        pedidoService.marcarComienzoDePreparacion(pedido1.getId());

        //when
        response = pedidoService.marcarPedidoListoParaRetirar(pedido1.getId());

        //then
        assertThat(response.getStatusCode()).isEqualTo(ACCEPTED);
        assertThat(response.getBody()).isEqualTo("Se ha marcado que el pedido está listo para retirar.");
    }
    @Test
    void noSePuedeMarcarListoParaRetirarAUnPedidoQueNoExiste() {
        //when
        response = pedidoService.marcarPedidoListoParaRetirar(1L);

        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("No existe el pedido al cual usted quiere marcar como disponible su retiro.");
    }

    @Test
    void noSePuedeMarcarListoParaRetirarAUnPedidoQueNoEstaEnPreparacion() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);

        //when
        response = pedidoService.marcarPedidoListoParaRetirar(pedido1.getId());

        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("No se puede marcar dicho pedido como lista para retirar ya que el mismo no se encuentra en preparación.");
    }

    @Test
    void sePuedeConfirmarRetiroAUnPedidoQueEstaListoParaRetirar() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);
        pedidoService.marcarComienzoDePreparacion(pedido1.getId());
        pedidoService.marcarPedidoListoParaRetirar(pedido1.getId());

        //when
        response = pedidoService.confirmarRetiroDelPedido(pedido1.getId());

        //then
        assertThat(response.getStatusCode()).isEqualTo(ACCEPTED);
        assertThat(response.getBody()).isEqualTo("Se ha confirmado el retiro del pedido.");
    }

    @Test
    void noSePuedeConfirmarRetiroAUnPedidoQueNoExiste() {
        //when
        response = pedidoService.confirmarRetiroDelPedido(1L);

        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("No existe el pedido al cual usted quiere confirmar el retiro.");
    }

    @Test
    void noSePuedeConfirmarRetiroAUnPedidoQueNoEstaListoParaRetirar() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);

        //when
        response = pedidoService.confirmarRetiroDelPedido(pedido1.getId());

        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("No se puede retirar dicho pedido ya que el mismo no se encuentra listo para retirar.");
    }

    @Test
    void sePuedeCancelarAUnPedidoQueEstaAguardandoPreparacion() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);

        //when
        response = pedidoService.cancelarPedido(pedido1.getId());

        //then
        assertThat(response.getStatusCode()).isEqualTo(ACCEPTED);
        assertThat(response.getBody()).isEqualTo("Se ha cancelado el pedido.");
    }

    @Test
    void noSePuedeCancelarAUnPedidoQueNoExiste() {
        //when
        response = pedidoService.cancelarPedido(1L);

        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("No existe el pedido que usted busca cancelar.");
    }

    @Test
    void cuandoSeCancelaUnPedidoEnAguardandoPreparacionSeDevulveElSaldo() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));

        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 9, "usaPdc", 0)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);
        pedidoService.confirmarPedido(infoPedidoDto);

        Dinero saldoPostConfirmarPedido = cliente.getSaldo();
        PuntosDeConfianza puntosPostConfirmarPedido = cliente.getPuntosDeConfianza();

        Collection<PedidoDto> pedidos = clienteService.obtenerPedidos(cliente.getId());

        //when
        for (PedidoDto entry: pedidos) {
            pedidoService.cancelarPedido(entry.getIdPedido());
        }

        //then
        Dinero saldoPostCancelacion = cliente.getSaldo();
        PuntosDeConfianza puntosPostCancelacion = cliente.getPuntosDeConfianza();

        assertThat(saldoPostConfirmarPedido).isEqualTo(new Dinero(100));
        assertThat(puntosPostConfirmarPedido).isEqualTo(new PuntosDeConfianza(0));
        assertThat(saldoPostCancelacion).isEqualTo(new Dinero(1000));
        // se le resta un 5% de los puntos de confianza
        assertThat(puntosPostCancelacion).isEqualTo(puntosPostConfirmarPedido.minus(puntosPostConfirmarPedido.multiply(0.05)));
    }

    @Test
    void cuandoSeCancelaUnPedidoEnPreparacionNoSeDevulveElSaldo() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));

        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 9, "usaPdc", 0)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);
        pedidoService.confirmarPedido(infoPedidoDto);

        Dinero saldoPostConfirmarPedido = cliente.getSaldo();
        PuntosDeConfianza puntosPostConfirmarPedido = cliente.getPuntosDeConfianza();

        Collection<PedidoDto> pedidos = clienteService.obtenerPedidos(cliente.getId());

        //when
        for (PedidoDto entry: pedidos) {
            pedidoService.marcarComienzoDePreparacion(entry.getIdPedido());
            pedidoService.cancelarPedido(entry.getIdPedido());
        }

        //then
        Dinero saldoPostCancelacion = cliente.getSaldo();
        PuntosDeConfianza puntosPostCancelacion = cliente.getPuntosDeConfianza();

        assertThat(saldoPostConfirmarPedido).isEqualTo(new Dinero(100));
        assertThat(puntosPostConfirmarPedido).isEqualTo(new PuntosDeConfianza(0));
        assertThat(saldoPostCancelacion).isEqualTo(new Dinero(100));
        // se le resta un 20% de los puntos de confianza
        assertThat(puntosPostCancelacion).isEqualTo(puntosPostConfirmarPedido.minus(puntosPostConfirmarPedido.multiply(0.2)));
    }

    @Test
    void cuandoSeCancelaUnPedidoListoParaRetirarNoSeDevulveElSaldoYSeLeSacanLosPuntosDeConfianza() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));

        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 9, "usaPdc", 0)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);
        pedidoService.confirmarPedido(infoPedidoDto);

        Dinero saldoPostConfirmarPedido = cliente.getSaldo();
        PuntosDeConfianza puntosPostConfirmarPedido = cliente.getPuntosDeConfianza();

        Collection<PedidoDto> pedidos = clienteService.obtenerPedidos(cliente.getId());

        //when
        for (PedidoDto entry: pedidos) {
            pedidoService.marcarComienzoDePreparacion(entry.getIdPedido());
            pedidoService.marcarPedidoListoParaRetirar(entry.getIdPedido());
            pedidoService.cancelarPedido(entry.getIdPedido());
        }

        //then
        Dinero saldoPostCancelacion = cliente.getSaldo();
        PuntosDeConfianza puntosPostCancelacion = cliente.getPuntosDeConfianza();

        assertThat(saldoPostConfirmarPedido).isEqualTo(new Dinero(100));
        assertThat(puntosPostConfirmarPedido).isEqualTo(new PuntosDeConfianza(0));
        assertThat(saldoPostCancelacion).isEqualTo(new Dinero(100));
        assertThat(puntosPostCancelacion).isEqualTo(new PuntosDeConfianza(0));
    }

    @Test
    void sePuedeMarcarPedidoDevueltoAUnPedidoQueEstaRetirado() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);
        pedidoService.marcarComienzoDePreparacion(pedido1.getId());
        pedidoService.marcarPedidoListoParaRetirar(pedido1.getId());
        pedidoService.confirmarRetiroDelPedido(pedido1.getId());

        //when
        response = pedidoService.devolverPedido(pedido1.getId());

        //then
        assertThat(response.getStatusCode()).isEqualTo(ACCEPTED);
        assertThat(response.getBody()).isEqualTo("Se ha confirmado la devolución del pedido.");
    }

    @Test
    void noSePuedeMarcarPedidoDevueltoAUnPedidoQueNoExiste() {
        //when
        response = pedidoService.devolverPedido(1L);

        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("No existe el pedido que usted busca devolver.");
    }

    @Test
    void noSePuedeMarcarPedidoDevueltoAUnPedidoQueNoEstaRetirado() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);

        //when
        response = pedidoService.devolverPedido(pedido1.getId());

        // then: "se lanza error"
        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("No se puede devolver dicho pedido ya que el mismo no se encontraba retirado.");
    }

    @Test
    void cuandoSeDevuelveUnPedidoSeLeDevuelveElSaldoAlClienteYElStockDeLosProductos() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));

        Long stockInicial = 10L;
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(stockInicial, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 9, "usaPdc", 0)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);
        pedidoService.confirmarPedido(infoPedidoDto);

        Dinero saldoPostConfirmarPedido = cliente.getSaldo();

        Collection<PedidoDto> pedidos = clienteService.obtenerPedidos(cliente.getId());

        //when
        for (PedidoDto entry: pedidos) {
            pedidoService.marcarComienzoDePreparacion(entry.getIdPedido());
            pedidoService.marcarPedidoListoParaRetirar(entry.getIdPedido());
            pedidoService.confirmarRetiroDelPedido(entry.getIdPedido());
            pedidoService.devolverPedido(entry.getIdPedido());
        }

        //then
        Dinero saldoPostCancelacion = cliente.getSaldo();

        Collection<ProductoDto> productosNegocio = negocioService.obtenerProductos(negocio.getId());
        for (ProductoDto entry: productosNegocio) {
            assertThat(entry.getStock()).isEqualTo(stockInicial);
        }

        assertThat(saldoPostConfirmarPedido).isEqualTo(new Dinero(100));
        assertThat(saldoPostCancelacion).isEqualTo(new Dinero(1000));
    }

    @Test
    void sePuedeComprarUnProductoConPdc() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));

        Long stockInicial = 10L;
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(stockInicial, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(20.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 9, "usaPdc", 0)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);
        pedidoService.confirmarPedido(infoPedidoDto);

        Collection<PedidoDto> pedidos = clienteService.obtenerPedidos(cliente.getId());
        for (PedidoDto entry: pedidos) {
            pedidoService.marcarComienzoDePreparacion(entry.getIdPedido());
            pedidoService.marcarPedidoListoParaRetirar(entry.getIdPedido());
            pedidoService.confirmarRetiroDelPedido(entry.getIdPedido());
        }
        PuntosDeConfianza pdcPostConfirmarPedido1 = cliente.getPuntosDeConfianza();


        Map<Long, Map<String, Object>> productos2 =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 1, "usaPdc", 1)
                );
        InfoPedidoDto infoPedidoDto2 = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos2);

        //when
        pedidoService.confirmarPedido(infoPedidoDto2);

        //then
        Dinero saldoPostConfirmarPedido = cliente.getSaldo();
        PuntosDeConfianza pdcPostConfirmarPedido = cliente.getPuntosDeConfianza();

        assertThat(saldoPostConfirmarPedido).isEqualTo(new Dinero(100));
        assertThat(pdcPostConfirmarPedido1).isEqualTo(new PuntosDeConfianza(180));
        assertThat(pdcPostConfirmarPedido).isEqualTo(new PuntosDeConfianza(160));

    }

    @Test
    void sePuedeComprarUnProductoConPdcYDinero() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));

        Long stockInicial = 10L;
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(stockInicial, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(40.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 1, "usaPdc", 0)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);
        pedidoService.confirmarPedido(infoPedidoDto);

        Collection<PedidoDto> pedidos = clienteService.obtenerPedidos(cliente.getId());
        for (PedidoDto entry: pedidos) {
            pedidoService.marcarComienzoDePreparacion(entry.getIdPedido());
            pedidoService.marcarPedidoListoParaRetirar(entry.getIdPedido());
            pedidoService.confirmarRetiroDelPedido(entry.getIdPedido());
        }
        PuntosDeConfianza pdcPostConfirmarPedido1 = cliente.getPuntosDeConfianza();


        Map<Long, Map<String, Object>> productos2 =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 1, "usaPdc", 1)
                );
        InfoPedidoDto infoPedidoDto2 = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos2);

        //when
        pedidoService.confirmarPedido(infoPedidoDto2);

        //then
        Dinero saldoPostConfirmarPedido = cliente.getSaldo();
        PuntosDeConfianza pdcPostConfirmarPedido = cliente.getPuntosDeConfianza();

        assertThat(saldoPostConfirmarPedido).isEqualTo(new Dinero(BigDecimal.valueOf(850.0)));
        assertThat(pdcPostConfirmarPedido1).isEqualTo(new PuntosDeConfianza(20));
        assertThat(pdcPostConfirmarPedido).isEqualTo(new PuntosDeConfianza(0));
    }
}