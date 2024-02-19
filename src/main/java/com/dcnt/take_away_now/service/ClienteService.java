package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.Cliente;
import com.dcnt.take_away_now.domain.Pedido;
import com.dcnt.take_away_now.domain.Plan;
import com.dcnt.take_away_now.dto.PedidoDto;
import com.dcnt.take_away_now.enums.EstadoDelPedido;
import com.dcnt.take_away_now.repository.ClienteRepository;
import com.dcnt.take_away_now.repository.PedidoRepository;
import com.dcnt.take_away_now.repository.PlanRepository;
import com.dcnt.take_away_now.repository.ProductoPedidoRepository;
import com.dcnt.take_away_now.value_object.Dinero;
import lombok.AllArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final PlanRepository planRepository;

    public Collection<Cliente> obtenerClientes() {
        return clienteRepository.findAll();
    }

    public String obtenerUsuario(Long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente).orElseThrow( () -> new RuntimeException("No existe el cliente en la base de datos."));
        return cliente.getUsuario();
    }

    public ResponseEntity<String> crearCliente(String usuario, LocalDateTime fechaDeCumpleanios) {
        Optional<Cliente> optionalCliente = clienteRepository.findByUsuario(usuario);
        if (optionalCliente.isPresent()) {
            return ResponseEntity.internalServerError().body("Ya existe un usuario con el nombre ingresado.");
        }

        this.clienteRepository.save(new Cliente(usuario, fechaDeCumpleanios));
        return ResponseEntity.ok().body("Cliente creado con éxito.");
    }

    public ResponseEntity<String> subscribirPlan(Long idCliente, Long idPlan) {

        // Corroboramos la existencia del cliente
        Optional<Cliente> optionalCliente = clienteRepository.findById(idCliente);
        if (optionalCliente.isEmpty()) {
            return ResponseEntity.internalServerError().body("No existe el cliente en la base de datos.");
        }

        // Corroboramos la existencia del plan
        Optional<Plan> optionalPlan = planRepository.findById(idPlan);
        if (optionalPlan.isEmpty()) {
            return ResponseEntity.internalServerError().body("No existe el plan en la base de datos.");
        }

        // Obtenemos cliente y plan
        Cliente cliente = optionalCliente.get();
        Plan plan = optionalPlan.get();

        // Corroboramos que el cliente no tenga el plan a suscribir
        if (cliente.getIdPlan() == idPlan) {
            return ResponseEntity.internalServerError().body("El cliente ya esta subscrito al plan.");
        }

        // Corroboramos que el cliente tenga saldo suficiente
        if (cliente.getSaldo().compareTo(plan.getPrecio()) < 0) {
            return ResponseEntity.internalServerError().body("El cliente no tiene saldo suficiente para suscribirse al plan.");
        }

        // Suscribimos al cliente al plan
        cliente.setSaldo(cliente.getSaldo().minus(plan.getPrecio()));
        cliente.setIdPlan(idPlan);
        cliente.setFechaSubscripcion(LocalDateTime.now());
        cliente.setPuntosDeConfianza(cliente.getPuntosDeConfianza().plus(plan.getPuntosDeConfianzaPorSubscripcion()));
        this.clienteRepository.save(cliente);
        return ResponseEntity.ok().body("Se ha suscrito correctamente al plan prime.");
    }

    public ResponseEntity<String> cargarSaldo(Long idCliente, BigDecimal saldoACargar) {
        Optional<Cliente> optionalCliente = clienteRepository.findById(idCliente);
        if (optionalCliente.isEmpty()) {
            return ResponseEntity.internalServerError().body("No existe el cliente en la base de datos.");
        }

        Cliente cliente = optionalCliente.get();
        cliente.setSaldo(cliente.getSaldo().plus(new Dinero(saldoACargar)));
        this.clienteRepository.save(cliente);
        return ResponseEntity.ok().body("Se han cargado correctamente a su saldo el monto de $" + saldoACargar + "." );
    }
    public void eliminarCliente(Long idCliente) {
        clienteRepository.deleteById(idCliente);
    }

    public Collection<PedidoDto> obtenerPedidos(Long idCliente) {
        // Corroboramos la existencia del cliente
        clienteRepository.findById(idCliente).orElseThrow( () -> new RuntimeException("No existe el cliente en la base de datos.") );

        return pedidoRepository.obtenerPedidosDelCliente(idCliente);
    }
}
