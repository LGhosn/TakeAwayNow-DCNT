package com.dcnt.take_away_now.controller;

import com.dcnt.take_away_now.domain.*;
import com.dcnt.take_away_now.dto.InfoPedidoDto;
import com.dcnt.take_away_now.dto.ProductoPedidoDto;
import com.dcnt.take_away_now.service.PedidoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static org.springframework.http.HttpStatus.ACCEPTED;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;


    /*****************
     *   Métodos Get *
     *****************/
    @GetMapping("/")
    public Collection<Pedido> obtenerPedidos() {
        return pedidoService.obtenerPedidos();
    }

    @GetMapping("/{idPedido}")
    public Collection<ProductoPedidoDto> obtenerDetalleDelPedido(@PathVariable Long idPedido) {
        return pedidoService.obtenerDetalleDelPedido(idPedido);
    }

    /******************
     *   Métodos Post *
     ******************/
    @PostMapping("/")
    public ResponseEntity<String> confirmarPedido(@RequestBody InfoPedidoDto infoPedido) {
        try {
            pedidoService.verificarPedido(infoPedido);
        } catch (RuntimeException e) {
            ResponseEntity.internalServerError().body(e.getMessage());
        }
        return  ResponseEntity.ok().body("El pedido fue confirmado correctamente.");
    }

    /*******************
     *   Métodos Patch *
     *******************/
    @PatchMapping("/{pedidoId}/marcarComienzoDePreparacion")
    public ResponseEntity<String> marcarComienzoDePreparacion(@PathVariable Long pedidoId) {
        try {
            pedidoService.marcarComienzoDePreparacion(pedidoId);
        } catch (RuntimeException e) {
            ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.status(ACCEPTED).body("Se ha marcado que el pedido está en comienzo de preparación.");
    }

    @PatchMapping("/{pedidoId}/marcarPedidoListoParaRetirar")
    public ResponseEntity<String> marcarPedidoListoParaRetirar(@PathVariable Long pedidoId) {
        try{
            pedidoService.marcarPedidoListoParaRetirar(pedidoId);
        } catch (RuntimeException e) {
            ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.status(ACCEPTED).body("Se ha marcado que el pedido está listo para retirar.");
    }

    @PatchMapping("/{pedidoId}/confirmarRetiroDelPedido")
    public ResponseEntity<String> confirmarRetiroDelPedido(@PathVariable Long pedidoId) {
        try {
            pedidoService.confirmarRetiroDelPedido(pedidoId);
        } catch (RuntimeException e) {
            ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.status(ACCEPTED).body("Se ha confirmado el retiro del pedido.");
    }

    @PatchMapping("/{pedidoId}/devolverPedido")
    public ResponseEntity<String> devolverPedido(@PathVariable Long pedidoId) {
        try {
            pedidoService.solicitarDevolucion(pedidoId);
        } catch (RuntimeException e) {
            ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.status(ACCEPTED).body("Se ha solicitado la devolución del pedido correctamente.");
    }

    @PatchMapping("/{pedidoId}/aceptarDevolucion")
    public ResponseEntity<String> aceptarDevolucion(@PathVariable Long pedidoId) {
        try {
            pedidoService.aceptarDevolucion(pedidoId);
        } catch (RuntimeException e) {
            ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.status(ACCEPTED).body("Se ha aceptado la devolución del pedido correctamente.");
    }

    @PatchMapping("/{pedidoId}/denegarDevolucion")
    public ResponseEntity<String> denegarDevolucion(@PathVariable Long pedidoId) {
        try {
            pedidoService.denegarDevolucion(pedidoId);
        } catch (RuntimeException e) {
            ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.status(ACCEPTED).body("Se ha denegado la devolución del pedido correctamente.");
    }

    @PatchMapping("/{pedidoId}/cancelarPedido")
    public ResponseEntity<String> cancelarPedido(@PathVariable Long pedidoId) {
        try {
            pedidoService.cancelarPedido(pedidoId);
        } catch (RuntimeException e) {
            ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.status(ACCEPTED).body("Se ha cancelado el pedido.");
    }
}
