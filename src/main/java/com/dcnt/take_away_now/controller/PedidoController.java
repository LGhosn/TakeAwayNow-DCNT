package com.dcnt.take_away_now.controller;

import com.dcnt.take_away_now.domain.*;
import com.dcnt.take_away_now.dto.InfoPedidoDto;
import com.dcnt.take_away_now.repository.*;
import com.dcnt.take_away_now.service.ClienteService;
import com.dcnt.take_away_now.service.PedidoService;
import com.dcnt.take_away_now.value_object.Dinero;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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

    /******************
     *   Métodos Post *
     ******************/
    @PostMapping("/")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> confirmarPedido(@RequestBody InfoPedidoDto infoPedido) {
        return pedidoService.verificarPedido(infoPedido);
    }

    /*******************
     *   Métodos Patch *
     *******************/
    @PatchMapping("/{pedidoId}/marcarComienzoDePreparacion")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> marcarComienzoDePreparacion(@PathVariable Long pedidoId) {
        return pedidoService.marcarComienzoDePreparacion(pedidoId);
    }

    @PatchMapping("/{pedidoId}/marcarPedidoListoParaRetirar")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> marcarPedidoListoParaRetirar(@PathVariable Long pedidoId) {
        return pedidoService.marcarPedidoListoParaRetirar(pedidoId);
    }

    @PatchMapping("/{pedidoId}/confirmarRetiroDelPedido")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> confirmarRetiroDelPedido(@PathVariable Long pedidoId) {
        return pedidoService.confirmarRetiroDelPedido(pedidoId);
    }

    @PatchMapping("/{pedidoId}/devolverPedido")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> devolverPedido(@PathVariable Long pedidoId) {
        return pedidoService.devolverPedido(pedidoId);
    }

    @PatchMapping("/{pedidoId}/cancelarPedido")
    public ResponseEntity<org.apache.hc.core5.http.HttpStatus> cancelarPedido(@PathVariable Long pedidoId) {
        return pedidoService.cancelarPedido(pedidoId);
    }
}
