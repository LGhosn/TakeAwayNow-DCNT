package com.dcnt.take_away_now.controller;

import com.dcnt.take_away_now.domain.Cliente;
import com.dcnt.take_away_now.dto.PedidoDto;
import com.dcnt.take_away_now.service.ClienteService;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        try {
            return clienteService.obtenerUsuario(idCliente);
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    @GetMapping("/corroborarExistencia/{usuario}")
    public ResponseEntity<Map<String, Object>> obtenerUsuario(@PathVariable String usuario) {
        Map<String, Object> response = new HashMap<>();
        Long idCliente = clienteService.corroborarExistencia(usuario);
        if ( idCliente > 0) {
            response.put("mensaje", "Hola de nuevo " + usuario + " !");
            response.put("id", idCliente);
        } else {
            response.put("mensaje", "No existe un cliente con ese usuario en la base de datos.");
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    /******************
     *   Métodos Post *
     ******************/
    @PostMapping("/")
    public ResponseEntity<String> crearCliente(@RequestParam String nombreUsuario) {
        try {
            clienteService.crearCliente(nombreUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Cliente creado con éxito.");
    }

    @PostMapping("/{idCliente}/cargaDeSaldo/{saldoACargar}")
    public ResponseEntity<String> cargarSaldo(@PathVariable Long idCliente, @PathVariable BigDecimal saldoACargar) {
        try {
            clienteService.cargarSaldo(idCliente, saldoACargar);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Se han cargado correctamente a su saldo el monto de $" + saldoACargar + "." );

    }

    @PatchMapping("/{idCliente}/establecerFechaDeNacimiento")
    public ResponseEntity<String> establecerFechaDeNacimiento(@PathVariable Long idCliente, @RequestParam int yyyy, @RequestParam int mm, @RequestParam int dd) {
        try {
            clienteService.establecerFechaDeNacimiento(idCliente, yyyy, mm, dd);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Fecha de nacimiento guardada correctamente.");
    }

    @PostMapping("/{idCliente}/obtenerPlanPrimee")
    public ResponseEntity<String> obtenerPlanPrime(@PathVariable Long idCliente) {
        try {
            clienteService.obtenerPlanPrime(idCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok("El plan Prime fue adquirido con éxito.");
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
