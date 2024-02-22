package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.*;
import com.dcnt.take_away_now.dto.InfoPedidoDto;
import com.dcnt.take_away_now.dto.InventarioRegistroDto;
import com.dcnt.take_away_now.dto.PedidoDto;
import com.dcnt.take_away_now.dto.ProductoDto;
import com.dcnt.take_away_now.repository.*;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.ResponseEntity;

import static com.dcnt.take_away_now.domain.Pedido.EstadoDelPedido.*;
import static org.springframework.http.HttpStatus.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
    @Autowired
    private PlanRepository planRepository;
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
        clienteService = new ClienteService(clienteRepository, pedidoRepository, productoPedidoRepository, planRepository);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));
        negocioService = new NegocioService(negocioRepository, inventarioRegistroRepository, productoRepository,pedidoRepository);
        pedidoService = new PedidoService(pedidoRepository, clienteRepository, negocioRepository,productoRepository, inventarioRegistroRepository,productoPedidoRepository, planRepository);
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
        pedidoService.confirmarPedido(infoPedidoDto);
        //then
        assertThat(negocio.getSaldo()).isEqualTo(new Dinero(9 * 100));
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
        pedidoService.verificarPedido(infoPedidoDto);

        //then
        assertThat(negocio.getSaldo()).isEqualTo(new Dinero(10 * 100));
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

        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(1L, negocio.getId(), productos);

        //when
        assertThatThrownBy(() ->
                pedidoService.verificarPedido(infoPedidoDto)
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No existe el cliente para el cual se busca verificar la integridad del pedido.");
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
        assertThatThrownBy(() ->
                pedidoService.verificarPedido(infoPedidoDto)
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No existe el negocio para el cual se busca verificar la integridad del pedido.");
    }

    @Test
    void sePuedeMarcarComienzoDePreparacionAUnPedidoQueEstaAguardandoPreparacion() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);

        //when
        pedidoService.marcarComienzoDePreparacion(pedido1.getId());

        assertThat(pedido1.getEstado()).isEqualTo(EN_PREPARACION);
    }

    @Test
    void noSePuedeMarcarComienzoDePreparacionAUnPedidoQueNoExiste() {
        //when
        assertThatThrownBy(() ->
                pedidoService.marcarComienzoDePreparacion(1L)
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No existe el pedido al cual usted quiere marcar su comienzo de preparación.");
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
        assertThatThrownBy(() ->
                pedidoService.marcarComienzoDePreparacion(pedido1.getId())
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No se puede comenzar a preparar dicho pedido ya que el mismo no se encuentra aguardando preparación.");
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
        pedidoService.marcarPedidoListoParaRetirar(pedido1.getId());

        //then
        assertThat(pedido1.getEstado()).isEqualTo(LISTO_PARA_RETIRAR);

    }
    @Test
    void noSePuedeMarcarListoParaRetirarAUnPedidoQueNoExiste() {
        //when
        assertThatThrownBy(() ->
                pedidoService.marcarPedidoListoParaRetirar(1L)
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No existe el pedido al cual usted quiere marcar como disponible su retiro.");
    }

    @Test
    void noSePuedeMarcarListoParaRetirarAUnPedidoQueNoEstaEnPreparacion() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);

        //when
        assertThatThrownBy(() ->
                pedidoService.marcarPedidoListoParaRetirar(pedido1.getId())
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No se puede marcar dicho pedido como lista para retirar ya que el mismo no se encuentra en preparación.");
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
        pedidoService.confirmarRetiroDelPedido(pedido1.getId());

        //then
        assertThat(pedido1.getEstado()).isEqualTo(RETIRADO);
    }

    @Test
    void noSePuedeConfirmarRetiroAUnPedidoQueNoExiste() {
        //when
        assertThatThrownBy(() ->
                pedidoService.confirmarRetiroDelPedido(1L)
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No existe el pedido al cual usted quiere confirmar el retiro.");
    }

    @Test
    void noSePuedeConfirmarRetiroAUnPedidoQueNoEstaListoParaRetirar() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);

        //when
        assertThatThrownBy(() ->
                pedidoService.confirmarRetiroDelPedido(pedido1.getId())
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No se puede retirar dicho pedido ya que el mismo no se encuentra listo para retirar.");
    }

    @Test
    void sePuedeCancelarAUnPedidoQueEstaAguardandoPreparacion() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);

        //when
        pedidoService.cancelarPedido(pedido1.getId());

        //then
        assertThat(pedido1.getEstado()).isEqualTo(CANCELADO);
    }

    @Test
    void noSePuedeCancelarAUnPedidoQueNoExiste() {
        //when
        assertThatThrownBy(() ->
                pedidoService.cancelarPedido(1L)
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No existe el pedido que usted busca cancelar.");
    }

    @Test
    void cuandoSeCancelaUnPedidoEnAguardandoPreparacionSeDevulveElSaldoYSeDisminuyenLosPdcEnUnCincoPorcientoDelTotalDeLaRecompenzaDelPedido() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));
        PuntosDeConfianza veintePdc = new PuntosDeConfianza(20.0);

        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), veintePdc,veintePdc);
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
        assertThat(puntosPostCancelacion).isEqualTo(puntosPostConfirmarPedido.minus(veintePdc.multiply(9).multiply(0.05)));
    }

    @Test
    void cuandoSeCancelaUnPedidoEnPreparacionNoSeDevulveElSaldoYDisminuyenLosPdcEnUnVeintePorcientoDelTotalDeLaRecompenzaDelPedido() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));
        PuntosDeConfianza veintePdc = new PuntosDeConfianza(20.0);

        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), veintePdc,veintePdc);negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
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
        assertThat(puntosPostCancelacion).isEqualTo(puntosPostConfirmarPedido.minus(veintePdc.multiply(9).multiply(0.20)));
    }

    @Test
    void cuandoSeCancelaUnPedidoListoParaRetirarNoSeDevulveElSaldoYSeLeSacanQuinientosPdc() {
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
        assertThat(puntosPostCancelacion).isEqualTo(new PuntosDeConfianza(-500));
    }

    @Test
    void sePuedeSolicitarDevolucionDeUnPedidoQueFueRetiradoHaceMenosDeCincoMinutos() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);
        pedidoService.marcarComienzoDePreparacion(pedido1.getId());
        pedidoService.marcarPedidoListoParaRetirar(pedido1.getId());
        pedidoService.confirmarRetiroDelPedido(pedido1.getId());

        //when
        pedidoService.solicitarDevolucion(pedido1.getId());

        //then
        assertThat(pedido1.getEstado()).isEqualTo(DEVOLUCION_SOLICITADA);
    }

    @Test
    void noSePuedeSolicitarDevolucionDeUnPedidoQueNoExiste() {
        //when
        assertThatThrownBy(() ->
                pedidoService.solicitarDevolucion(1L)
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No existe el pedido que usted busca solicitar su devolución.");
    }

    @Test
    void noSePuedeSolicitarDevolucionDeUnPedidoQueNoEstaRetirado() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        Pedido pedido1 = new Pedido(negocio, cliente);
        pedidoRepository.save(pedido1);

        //when
        assertThatThrownBy(() ->
                pedidoService.solicitarDevolucion(pedido1.getId())
        )

        // then: *se lanza error*
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No se puede solicitar la devolución de dicho pedido ya que el mismo no se encontraba retirado.");
    }

    @Test
    void cuandoSeAceptaLaDevolucionDeUnPedidoSeLeDevuelveElSaldoAlClienteYElStockDeLosProductos() {
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
            pedidoService.solicitarDevolucion(entry.getIdPedido());
            pedidoService.aceptarDevolucion(entry.getIdPedido());
        }

        //then
        Dinero saldoPostDevolucionAceptada = cliente.getSaldo();

        Collection<ProductoDto> productosNegocio = negocioService.obtenerProductos(negocio.getId());
        for (ProductoDto entry: productosNegocio) {
            assertThat(entry.getStock()).isEqualTo(stockInicial);
        }

        assertThat(saldoPostConfirmarPedido).isEqualTo(new Dinero(100));
        assertThat(saldoPostDevolucionAceptada).isEqualTo(new Dinero(1000));
    }

    @Test
    void cuandoSeDeniegaLaDevolucionDeUnPedidoNoSeLeDevuelveElSaldoAlClienteNiElStockDeLosProductos() {
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
            pedidoService.solicitarDevolucion(entry.getIdPedido());
            pedidoService.denegarDevolucion(entry.getIdPedido());
        }

        //then
        Dinero saldoPostDevolucionDenegada = cliente.getSaldo();

        Collection<ProductoDto> productosNegocio = negocioService.obtenerProductos(negocio.getId());
        for (ProductoDto entry: productosNegocio) {
            assertThat(entry.getStock()).isEqualTo(10L - 9);
        }

        assertThat(saldoPostConfirmarPedido).isEqualTo(new Dinero(100));
        assertThat(saldoPostDevolucionDenegada).isEqualTo(new Dinero(100));
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
        cliente.setPuntosDeConfianza(new PuntosDeConfianza(100));
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));

        Long stockInicial = 10L;
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(stockInicial, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(40.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        negocioService.crearProducto(negocio.getId(), "Coca",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");
        Optional<Producto> coca = productoRepository.findByNombre("Coca");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 1, "usaPdc", 0),
                        coca.get().getId(), Map.of("cantidad", 1, "usaPdc", 1)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);
        pedidoService.confirmarPedido(infoPedidoDto);

        Collection<PedidoDto> pedidos = clienteService.obtenerPedidos(cliente.getId());
        for (PedidoDto entry: pedidos) {
            pedidoService.marcarComienzoDePreparacion(entry.getIdPedido());
            pedidoService.marcarPedidoListoParaRetirar(entry.getIdPedido());
            pedidoService.confirmarRetiroDelPedido(entry.getIdPedido());
        }

        //then
        Dinero saldoPostConfirmarPedido = cliente.getSaldo();
        PuntosDeConfianza pdcPostConfirmarPedido = cliente.getPuntosDeConfianza();

        assertThat(saldoPostConfirmarPedido).isEqualTo(new Dinero(BigDecimal.valueOf(900)));
        assertThat(pdcPostConfirmarPedido).isEqualTo(new PuntosDeConfianza(100));
    }

    @Test
    void veinticincoPorcientoDeDescuentoYMilPuntosDeRegaloPorBeneficioDeCumpleanios() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));
        LocalDate hoy = LocalDate.now();
        clienteService.establecerFechaDeNacimiento(cliente.getId(), 1987,hoy.getMonth().getValue(), hoy.getDayOfMonth());

        Long stockInicial = 10L;
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(stockInicial, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(40.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 1, "usaPdc", 0)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);

        //when
        pedidoService.confirmarPedido(infoPedidoDto);
        Collection<PedidoDto> pedidos = clienteService.obtenerPedidos(cliente.getId());

        for (PedidoDto entry: pedidos) {
            pedidoService.marcarComienzoDePreparacion(entry.getIdPedido());
            pedidoService.marcarPedidoListoParaRetirar(entry.getIdPedido());
            pedidoService.confirmarRetiroDelPedido(entry.getIdPedido());
            pedidoService.solicitarDevolucion(entry.getIdPedido());
            pedidoService.denegarDevolucion(entry.getIdPedido());
        }

        //then
        Dinero saldoPostConfirmarRetiro = cliente.getSaldo();
        PuntosDeConfianza pdcPostConfirmarRetiro = cliente.getPuntosDeConfianza();

        assertThat(pdcPostConfirmarRetiro).isEqualTo(new PuntosDeConfianza(1020));
        assertThat(saldoPostConfirmarRetiro).isEqualTo(new Dinero(925));
    }

    @Test
    void noSePuedeReutilizarElBeneficioDeCumpleanios() {
        //given
        Cliente cliente = new Cliente("Messi");
        clienteRepository.save(cliente);
        clienteService.cargarSaldo(cliente.getId(), BigDecimal.valueOf(1000));
        LocalDate hoy = LocalDate.now();
        clienteService.establecerFechaDeNacimiento(cliente.getId(), 1987,hoy.getMonth().getValue(), hoy.getDayOfMonth());

        Long stockInicial = 10L;
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(stockInicial, new Dinero(100), new PuntosDeConfianza(20.0),new PuntosDeConfianza(40.0));
        negocioService.crearProducto(negocio.getId(), "Alfajor",inventarioRegistroDto);
        Optional<Producto> alfajor = productoRepository.findByNombre("Alfajor");

        Map<Long, Map<String, Object>> productos =
                Map.of(
                        alfajor.get().getId(), Map.of("cantidad", 1, "usaPdc", 0)
                );
        InfoPedidoDto infoPedidoDto = new InfoPedidoDto(cliente.getId(), negocio.getId(), productos);

        //when
        pedidoService.confirmarPedido(infoPedidoDto);
        Collection<PedidoDto> pedidos = clienteService.obtenerPedidos(cliente.getId());
        for (PedidoDto entry: pedidos) {
            pedidoService.marcarComienzoDePreparacion(entry.getIdPedido());
            pedidoService.marcarPedidoListoParaRetirar(entry.getIdPedido());
            pedidoService.confirmarRetiroDelPedido(entry.getIdPedido());
            pedidoService.solicitarDevolucion(entry.getIdPedido());
            pedidoService.denegarDevolucion(entry.getIdPedido());
        }

        pedidoService.confirmarPedido(infoPedidoDto);
        pedidos = clienteService.obtenerPedidos(cliente.getId());
        for (PedidoDto entry: pedidos) {
            if (entry.getEstado() != DEVOLUCION_DENEGADA) {
                pedidoService.marcarComienzoDePreparacion(entry.getIdPedido());
                pedidoService.marcarPedidoListoParaRetirar(entry.getIdPedido());
                pedidoService.confirmarRetiroDelPedido(entry.getIdPedido());
                pedidoService.solicitarDevolucion(entry.getIdPedido());
                pedidoService.denegarDevolucion(entry.getIdPedido());
            }
        }

        //then
        Dinero saldoPostConfirmarRetiro = cliente.getSaldo();
        PuntosDeConfianza pdcPostConfirmarRetiro = cliente.getPuntosDeConfianza();

        assertThat(pdcPostConfirmarRetiro).isEqualTo(new PuntosDeConfianza(1040));
        assertThat(saldoPostConfirmarRetiro).isEqualTo(new Dinero(825));
    }

}