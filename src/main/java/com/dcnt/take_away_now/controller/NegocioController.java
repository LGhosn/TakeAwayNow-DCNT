package com.dcnt.take_away_now.controller;

import com.dcnt.take_away_now.domain.Cliente;
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
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.ACCEPTED;

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

    @GetMapping("/negociosAbiertos")
    public Collection<Negocio> obtenerNegociosAbiertos() {
        return negocioService.obtenerNegociosAbiertos();
    }

    @GetMapping("/negociosCerrados")
    public Collection<Negocio> obtenerNegociosCerrados() {
        return negocioService.obtenerNegociosCerrados();
    }

    @GetMapping("/{idNegocio}/pedidos/")
    public Collection<PedidoDto> obtenerPedidos(@PathVariable Long idNegocio) {
        return negocioService.obtenerPedidos(idNegocio);
    }

    @GetMapping("/corroborarExistencia/{nombre}")
    public ResponseEntity<Map<String, Object>> obtenerNegocio(@PathVariable String nombre) {
        Long idNegocio = negocioService.corroborarExistencia(nombre);
        Map<String, Object> response = new HashMap<>();
        if (idNegocio > 0) {
            response.put("mensaje", "A laburar " + nombre +"!");
            response.put("id", idNegocio);
        } else {
            response.put("mensaje", "No existe un negocio con ese nombre en la base de datos.");
            ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{idNegocio}")
    public Negocio obtenerInfoNegocio(@PathVariable Long idNegocio) {
        return negocioService.obtenerInfoNegocio(idNegocio);
    }

    /******************
     *   Métodos Post *
     ******************/
    @PostMapping("/")
    public ResponseEntity<String> crearNegocio(
            @RequestParam String nombre,
            @RequestParam DayOfWeek diaDeApertura,
            @RequestParam DayOfWeek diaDeCierre,
            @RequestParam int horaApertura,
            @RequestParam int minutoApertura,
            @RequestParam int horaCierre,
            @RequestParam int minutoCierre
    ) {
        try {
            negocioService.crearNegocio(nombre, diaDeApertura, diaDeCierre, horaApertura, minutoApertura, horaCierre, minutoCierre);
        } catch (RuntimeException e) {
            ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Negocio creado correctamente.");
    }
    @PostMapping("/{negocioId}/productos/")
    public ResponseEntity<String> crearProducto(
            @PathVariable Long negocioId,
            @RequestParam String nombreDelProducto,
            @RequestParam Long stock,
            @RequestParam BigDecimal precio,
            @RequestParam Double recompensaPuntosDeConfianza,
            @RequestParam Double precioPdc
    ) {
        try {
            negocioService.crearProducto(negocioId, nombreDelProducto, new InventarioRegistroDto(stock, new Dinero(precio), new PuntosDeConfianza(recompensaPuntosDeConfianza), new PuntosDeConfianza(precioPdc)));
        } catch (RuntimeException e) {
            ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Se ha creado el producto correctamente.");
    }
    /*******************
     *   Métodos Patch *
     *******************/
    @PatchMapping("/{negocioId}/productos/{productoId}")
    public ResponseEntity<String> modificarProducto(
            @PathVariable Long negocioId,
            @PathVariable Long productoId,
            @RequestParam Long stock,
            @RequestParam BigDecimal precio,
            @RequestParam Double recompensaPuntosDeConfianza,
            @RequestParam Double precioPdc
    ) {
        try {
            negocioService.modificarInventarioRegistro(negocioId, productoId, stock, precio, recompensaPuntosDeConfianza, precioPdc);
        } catch (RuntimeException e ) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.status(ACCEPTED).body("El producto fue modificado correctamente.");
    }

    @PatchMapping("/{negocioId}/horariosDeTrabajo")
    public ResponseEntity<String> modificarHorariosDelNegocio(
            @PathVariable Long negocioId,
            @RequestParam int horaApertura,
            @RequestParam int minutoApertura,
            @RequestParam int horaCierre,
            @RequestParam int minutoCierre
    ) {
        try {
            negocioService.modificarHorariosDelNegocio(negocioId, horaApertura, minutoApertura, horaCierre, minutoCierre);
        } catch (RuntimeException e ) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.accepted().body("Los horarios fueron modificados correctamente.");
    }

    @PatchMapping("/{negocioId}/diasDeTrabajo")
    public ResponseEntity<String> modificarDiasDeAperturaDelNegocio(
            @PathVariable Long negocioId,
            @RequestParam DayOfWeek diaDeApertura,
            @RequestParam DayOfWeek diaDeCierre
    ) {
        try {
            negocioService.modificarDiasDeAperturaDelNegocio(negocioId, diaDeApertura, diaDeCierre);
        } catch(RuntimeException e ) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.accepted().body("Los dias de apertura y cierre fueron modificados correctamente.");
    }

    /*******************
    *   Métodos Delete *
    ********************/
    @DeleteMapping("/{negocioId}/productos/{productoId}")
    public ResponseEntity<String> eliminarProducto(
            @PathVariable Long negocioId,
            @PathVariable Long productoId
    ) {
        try {
            negocioService.eliminarProducto(negocioId, productoId);
        } catch (RuntimeException e) {
            return  ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Producto eliminado correctamente.");
    }
}
