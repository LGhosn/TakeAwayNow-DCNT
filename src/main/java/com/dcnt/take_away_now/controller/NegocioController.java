package com.dcnt.take_away_now.controller;

import com.dcnt.take_away_now.domain.Negocio;
import com.dcnt.take_away_now.dto.InventarioRegistroDto;
import com.dcnt.take_away_now.dto.PedidoDto;
import com.dcnt.take_away_now.dto.ProductoDto;
import com.dcnt.take_away_now.service.NegocioService;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Collection;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/negocios")
public class NegocioController {
    private final NegocioService negocioService;

    public NegocioController(NegocioService negocioService) {
        this.negocioService = negocioService;
    }

    /*****************
     *   Métodos Get *
     *****************/
    @GetMapping("/{negocioId}/productos")
    public Collection<ProductoDto> obtenerProductos(@PathVariable Long negocioId) {
        return negocioService.obtenerProductos(negocioId);
    }

    @GetMapping("/")
    public Collection<Negocio> obtenerNegocios() {
        return negocioService.obtenerNegocios();
    }

    @GetMapping("/{idNegocio}/pedidos/")
    public Collection<PedidoDto> obtenerPedidos(@PathVariable Long idNegocio) {
        return negocioService.obtenerPedidos(idNegocio);
    }

    /******************
     *   Métodos Post *
     ******************/
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> crearNegocio(
            @RequestParam String nombre,
            @RequestParam DayOfWeek diaDeApertura,
            @RequestParam DayOfWeek diaDeCierre,
            @RequestParam int horaApertura,
            @RequestParam int minutoApertura,
            @RequestParam int horaCierre,
            @RequestParam int minutoCierre
    ) {
        return negocioService.crearNegocio(nombre, diaDeApertura, diaDeCierre, horaApertura, minutoApertura, horaCierre, minutoCierre);
    }
    @PostMapping("/{negocioId}/productos/")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> crearProducto(
            @PathVariable Long negocioId,
            @RequestParam String nombreDelProducto,
            @RequestParam Long stock,
            @RequestParam BigDecimal precio,
            @RequestParam Double recompensaPuntosDeConfianza
    ) {
        return negocioService.crearProducto(negocioId, nombreDelProducto, new InventarioRegistroDto(stock, new Dinero(precio), new PuntosDeConfianza(recompensaPuntosDeConfianza)));
    }
    /*******************
     *   Métodos Patch *
     *******************/
    @PatchMapping("/{negocioId}/productos/{productoId}")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> modificarProducto(
            @PathVariable Long negocioId,
            @PathVariable Long productoId,
            @RequestParam Long stock,
            @RequestParam BigDecimal precio,
            @RequestParam Double recompensaPuntosDeConfianza
    ) {
        return negocioService.modificarInventarioRegistro(negocioId, productoId, stock, precio, recompensaPuntosDeConfianza);
    }

    @PatchMapping("/{negocioId}/horariosDeTrabajo")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> modificarHorariosDelNegocio(
            @PathVariable Long negocioId,
            @RequestParam int horaApertura,
            @RequestParam int minutoApertura,
            @RequestParam int horaCierre,
            @RequestParam int minutoCierre
    ) {
        return negocioService.modificarHorariosDelNegocio(negocioId, horaApertura, minutoApertura, horaCierre, minutoCierre);
    }

    @PatchMapping("/{negocioId}/diasDeTrabajo")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> modificarDiasDeAperturaDelNegocio(
            @PathVariable Long negocioId,
            @RequestParam DayOfWeek diaDeApertura,
            @RequestParam DayOfWeek diaDeCierre
    ) {
        return negocioService.modificarDiasDeAperturaDelNegocio(negocioId, diaDeApertura, diaDeCierre);
    }

    /*******************
    *   Métodos Delete *
    ********************/
    @DeleteMapping("/{negocioId}/productos/{productoId}")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> eliminarProducto(
            @PathVariable Long negocioId,
            @PathVariable Long productoId
    ) {
        return negocioService.eliminarProducto(negocioId, productoId);
    }
}
