package com.dcnt.take_away_now.controller;

import com.dcnt.take_away_now.domain.Cliente;
import com.dcnt.take_away_now.dto.PedidoDto;
import com.dcnt.take_away_now.service.ClienteService;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /*****************
     *   Métodos Get *
     *****************/
    @GetMapping("/")
    public Collection<Cliente> obtenerClientes() {
        return clienteService.obtenerClientes();
    }

    @GetMapping("/{idCliente}")
    public Cliente obtenerInfoCliente(@PathVariable Long idCliente) {
        return clienteService.obtenerInfoCliente(idCliente);
    }

    @GetMapping("/{idCliente}/pedidos/")
    public Collection<PedidoDto> obtenerPedidos(@PathVariable Long idCliente) {
        return clienteService.obtenerPedidos(idCliente);
    }

    @GetMapping("/{idCliente}/usuario")
    public String obtenerUsuario(@PathVariable Long idCliente) {
        return clienteService.obtenerUsuario(idCliente);
    }

    @GetMapping("/corroborarExistencia/{usuario}")
    public ResponseEntity<String> obtenerUsuario(@PathVariable String usuario) {
        return clienteService.corroborarExistencia(usuario);
    }

    /******************
     *   Métodos Post *
     ******************/
    @PostMapping("/")
    public ResponseEntity<String> crearCliente(@RequestParam String nombreUsuario) {
        return clienteService.crearCliente(nombreUsuario);
    }

    @PostMapping("/{idCliente}/cargaDeSaldo/{saldoACargar}")
    public ResponseEntity<String> cargarSaldo(@PathVariable Long idCliente, @PathVariable BigDecimal saldoACargar) {
        return clienteService.cargarSaldo(idCliente, saldoACargar);
    }

    /*******************
     *   Métodos Patch *
     *******************/

    /*******************
    *   Métodos Delete *
    ********************/
    @DeleteMapping("/{clienteId}")
    public void eliminarCliente(@PathVariable Long clienteId) {
        clienteService.eliminarCliente(clienteId);
    }
}
