package com.dcnt.take_away_now.controller;

import com.dcnt.take_away_now.domain.*;
import com.dcnt.take_away_now.dto.InfoPedidoDto;
import com.dcnt.take_away_now.dto.ProductoPedidoDto;
import com.dcnt.take_away_now.service.PedidoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

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
        return pedidoService.verificarPedido(infoPedido);
    }

    /*******************
     *   Métodos Patch *
     *******************/
    @PatchMapping("/{pedidoId}/marcarComienzoDePreparacion")
    public ResponseEntity<String> marcarComienzoDePreparacion(@PathVariable Long pedidoId) {
        return pedidoService.marcarComienzoDePreparacion(pedidoId);
    }

    @PatchMapping("/{pedidoId}/marcarPedidoListoParaRetirar")
    public ResponseEntity<String> marcarPedidoListoParaRetirar(@PathVariable Long pedidoId) {
        return pedidoService.marcarPedidoListoParaRetirar(pedidoId);
    }

    @PatchMapping("/{pedidoId}/confirmarRetiroDelPedido")
    public ResponseEntity<String> confirmarRetiroDelPedido(@PathVariable Long pedidoId) {
        return pedidoService.confirmarRetiroDelPedido(pedidoId);
    }

    @PatchMapping("/{pedidoId}/devolverPedido")
    public ResponseEntity<String> devolverPedido(@PathVariable Long pedidoId) {
        return pedidoService.solicitarDevolucion(pedidoId);
    }

    @PatchMapping("/{pedidoId}/aceptarDevolucion")
    public ResponseEntity<String> aceptarDevolucion(@PathVariable Long pedidoId) {
        return pedidoService.aceptarDevolucion(pedidoId);
    }

    @PatchMapping("/{pedidoId}/denegarDevolucion")
    public ResponseEntity<String> denegarDevolucion(@PathVariable Long pedidoId) {
        return pedidoService.denegarDevolucion(pedidoId);
    }

    @PatchMapping("/{pedidoId}/cancelarPedido")
    public ResponseEntity<String> cancelarPedido(@PathVariable Long pedidoId) {
        return pedidoService.cancelarPedido(pedidoId);
    }
}
