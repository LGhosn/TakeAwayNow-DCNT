package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.*;
import com.dcnt.take_away_now.dto.InventarioRegistroDto;
import com.dcnt.take_away_now.dto.ProductoDto;
import com.dcnt.take_away_now.repository.*;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

//@ExtendWith(MockitoExtension.class)
@DataJpaTest
class NegocioServiceTest {
    @Autowired
    private NegocioRepository negocioRepository;
    @Autowired
    private InventarioRegistroRepository inventarioRegistroRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    private NegocioService negocioService;
    private DayOfWeek DiaDeApertura;
    private DayOfWeek DiaDeCierre;
    private int HoraApertura;
    private int MinutoApertura;
    private int HoraCierre;
    private int MinutoCierre;
    private String nombrePaseoColon;
    @BeforeEach
    void setUp() {
        DiaDeApertura = DayOfWeek.MONDAY;
        DiaDeCierre = DayOfWeek.FRIDAY;
        negocioService = new NegocioService(negocioRepository, inventarioRegistroRepository, productoRepository,pedidoRepository);
        HoraApertura = 9;
        MinutoApertura = 0;
        HoraCierre = 18;
        MinutoCierre = 0;
        nombrePaseoColon = "Buffet Paseo Colon";
    }

    @Test
    void sePuedeCrearNegocioNuevo() {
        //when
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        Optional<Negocio> negocio = negocioRepository.findByNombre(nombrePaseoColon);
        //then
        assertThat(negocio.isPresent()).isTrue();
    }

    @Test
    void noSePuedeCrearNegocioExistente() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        //when
         ResponseEntity<HttpStatus> status = negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        //then
        assertThat(status).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    void seObtienenNegociosExistentes() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        negocioService.crearNegocio("Buffet Las Heras", DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        negocioService.crearNegocio("Buffet Ciudad Universitaria", DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        //when
        Collection<Negocio> negocios = negocioService.obtenerNegocios();
        //then
        assertThat(negocios.size()).isEqualTo(3);
        assertThat(existeNegocio(negocios, nombrePaseoColon)).isTrue();
        assertThat(existeNegocio(negocios, "Buffet Las Heras")).isTrue();
        assertThat(existeNegocio(negocios, "Buffet Ciudad Universitaria")).isTrue();
    }

    private boolean existeNegocio(Collection<Negocio> negocios, String nombreNegocio) {
        return negocios.stream()
                .anyMatch(cliente -> cliente.getNombre().equals(nombreNegocio));
    }

    @Test
    void sePuedeCrearProductoNuevoEnNegocioExistente() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0));
        Optional<Negocio> negocio = negocioRepository.findByNombre(nombrePaseoColon);
        //when
        negocioService.crearProducto(negocio.get().getId(), "Pancho",inventarioRegistroDto);
        //then
        Optional<Producto> producto = productoRepository.findByNombre("Pancho");
        boolean existeInventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(negocio.get(), producto.get()).isPresent();
        assertThat(producto).isPresent();
        assertThat(existeInventarioRegistro).isTrue();
    }
    @Test
    void noSePuedeCrearProductoYaExistenteEnNegocioExistente() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0));
        Optional<Negocio> negocio = negocioRepository.findByNombre(nombrePaseoColon);
        negocioService.crearProducto(negocio.get().getId(), "Pancho",inventarioRegistroDto);
        //when
        ResponseEntity<HttpStatus> status= negocioService.crearProducto(negocio.get().getId(), "Pancho",inventarioRegistroDto);
        //then
        assertThat(status).isEqualTo(ResponseEntity.internalServerError().build());
    }
    @Test
    void noSePuedeCrearProductoNuevoEnNegocioQueNoExiste() {
        //given
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0));
        //when
        ResponseEntity<HttpStatus> status= negocioService.crearProducto(1L, "Pancho",inventarioRegistroDto);
        //then
        assertThat(status).isEqualTo(ResponseEntity.notFound().build());
    }
    @Test
    void eliminarProducto() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0));
        Optional<Negocio> negocio = negocioRepository.findByNombre(nombrePaseoColon);
        negocioService.crearProducto(negocio.get().getId(), "Pancho",inventarioRegistroDto);
        Optional<Producto> producto = productoRepository.findByNombre("Pancho");
        //when
        negocioService.eliminarProducto(negocio.get().getId(), producto.get().getId());
        //then
        boolean existeInventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(negocio.get(), producto.get()).isPresent();
        boolean existeProducto = productoRepository.findByNombre("Pancho").isPresent();
        assertThat(existeInventarioRegistro).isFalse();
        assertThat(existeProducto).isFalse();

    }

    @Test
    void obtenerProductos() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0));
        Optional<Negocio> negocio = negocioRepository.findByNombre(nombrePaseoColon);
        negocioService.crearProducto(negocio.get().getId(), "Pancho",inventarioRegistroDto);
        negocioService.crearProducto(negocio.get().getId(), "Coca Cola",inventarioRegistroDto);
        negocioService.crearProducto(negocio.get().getId(), "Alfajor",inventarioRegistroDto);
        //when
        Collection<ProductoDto> productos = negocioService.obtenerProductos(negocio.get().getId());
        //then
        assertThat(productos.size()).isEqualTo(3);
        assertThat(existeProducto(productos, "Pancho")).isTrue();
        assertThat(existeProducto(productos, "Coca Cola")).isTrue();
        assertThat(existeProducto(productos, "Alfajor")).isTrue();
    }

    private boolean existeProducto(Collection<ProductoDto> productos, String nombreProducto) {
        return productos.stream()
                .anyMatch(cliente -> cliente.getNombre().equals(nombreProducto));
    }

    @Test
    void noSePuedeobtenerProductosDeUnNegocioQueNoExiste() {
        // when: "Se intenta obtener productos de un negocio que no existe"
        assertThatThrownBy(
                () -> {
                    negocioService.obtenerProductos(1L);
                }
        )
        // then: "se lanza error"
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("No existe el negocio al cual se solicitó obtener sus productos.");
    }

    @Test
    void sePuedeModificarInventarioRegistro() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0));
        Optional<Negocio> negocio = negocioRepository.findByNombre(nombrePaseoColon);
        negocioService.crearProducto(negocio.get().getId(), "Pancho",inventarioRegistroDto);
        Optional<Producto> producto = productoRepository.findByNombre("Pancho");
        //when
        negocioService.modificarInventarioRegistro(negocio.get().getId(), producto.get().getId(), 20L, BigDecimal.valueOf(150), 25.0);
        //then
        Optional<InventarioRegistro> optInventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(negocio.get(), producto.get());
        assertThat(optInventarioRegistro.get().getStock()).isEqualTo(20L);
        assertThat(optInventarioRegistro.get().getPrecio()).isEqualTo(new Dinero(150));
        assertThat(optInventarioRegistro.get().getRecompensaPuntosDeConfianza()).isEqualTo(new PuntosDeConfianza(25.0));
    }

    @Test
    void noSePuedeModificarInventarioRegistroDeUnNegocioQueNoExiste() {
        // when: "Se intenta modificar un inventarioRegistro de un negocio que no existe"
        assertThatThrownBy(
                () -> {
                    negocioService.modificarInventarioRegistro(1L,1L,20L,BigDecimal.valueOf(150), 25.0);
                }
        )
        // then: "se lanza error"
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("No existe el negocio al cual se solicitó modificar uno de sus productos.");
    }
    @Test
    void noSePuedeModificarInventarioRegistroDeUnProductoQueNoExiste() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0));
        Optional<Negocio> negocio = negocioRepository.findByNombre(nombrePaseoColon);
        // when: "Se intenta modificar un inventarioRegistro de un producto que no existe"
        assertThatThrownBy(
                () -> {
                    negocioService.modificarInventarioRegistro(negocio.get().getId(),1L,20L,BigDecimal.valueOf(150), 25.0);
                }
        )
        // then: "se lanza error"
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("No existe el producto al cual se solicitó modificar.");
    }

    @Test
    void noSePuedeModificarInventarioRegistroDeUnProductoQueNoTieneRelacionConUnNegocio() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        negocioService.crearNegocio("Buffet Las Heras", DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20.0));
        Optional<Negocio> paseoColon = negocioRepository.findByNombre(nombrePaseoColon);
        negocioService.crearProducto(paseoColon.get().getId(), "Pancho",inventarioRegistroDto);
        Optional<Producto> producto = productoRepository.findByNombre("Pancho");
        Optional<Negocio> lasHeras = negocioRepository.findByNombre("Buffet Las Heras");

        // when: "Se intenta modificar un inventarioRegistro de un producto que no tiene relacion con el negocio"
        ResponseEntity<HttpStatus> status = negocioService.modificarInventarioRegistro(lasHeras.get().getId(),producto.get().getId(), 20L,BigDecimal.valueOf(150), 25.0);

        //then
        assertThat(status).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    void sePuedeModificarHorariosDelNegocio() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        Optional<Negocio> paseoColon = negocioRepository.findByNombre(nombrePaseoColon);

        // when
        negocioService.modificarHorariosDelNegocio(paseoColon.get().getId(), 14, 30, 21, 0);

        //then
        paseoColon = negocioRepository.findByNombre(nombrePaseoColon);
        assertThat(paseoColon.get().horarioDeApertura).isEqualTo(LocalTime.of(14, 30, 0, 0));
        assertThat(paseoColon.get().horarioDeCierre).isEqualTo(LocalTime.of(21, 0, 0, 0));
    }

    @Test
    void noSePuedeModificarHorariosDelNegocioCuandoElHorarioDeAperturaEsMayorAlDeCierre() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        Optional<Negocio> paseoColon = negocioRepository.findByNombre(nombrePaseoColon);

        // when
        ResponseEntity<HttpStatus> status = negocioService.modificarHorariosDelNegocio(paseoColon.get().getId(), 21, 30, 14, 0);

        //then
        assertThat(status).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    void noSePuedeModificarHorariosDeUnNegocioQueNoExiste() {
        // when
        ResponseEntity<HttpStatus> status = negocioService.modificarHorariosDelNegocio(1L, 14, 30, 21, 0);

        //then
        assertThat(status).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    void sePuedeModificarDiasDelNegocio() {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        Optional<Negocio> paseoColon = negocioRepository.findByNombre(nombrePaseoColon);

        // when
        negocioService.modificarDiasDeAperturaDelNegocio(paseoColon.get().getId(), DayOfWeek.THURSDAY, DayOfWeek.SATURDAY);

        //then
        paseoColon = negocioRepository.findByNombre(nombrePaseoColon);
        assertThat(paseoColon.get().diaDeApertura).isEqualTo( DayOfWeek.THURSDAY);
        assertThat(paseoColon.get().diaDeCierre).isEqualTo( DayOfWeek.SATURDAY);
    }

    @Test
    void noSePuedeModificarDiasDeUnNegocioQueNoExiste() {
        // when
        ResponseEntity<HttpStatus> status = negocioService.modificarDiasDeAperturaDelNegocio(1L, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY);

        //then
        assertThat(status).isEqualTo(ResponseEntity.badRequest().build());
    }

}
