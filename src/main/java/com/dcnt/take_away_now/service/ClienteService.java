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
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@AllArgsConstructor
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoPedidoRepository productoPedidoRepository;
    private final PlanRepository planRepository;

    public Collection<Cliente> obtenerClientes() {
        return clienteRepository.findAll();
    }

    public String obtenerUsuario(Long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente).orElseThrow( () -> new RuntimeException("No existe el cliente en la base de datos."));
        return cliente.getUsuario();
    }
    public ResponseEntity<String> crearCliente(String usuario) {
        Optional<Cliente> optionalCliente = clienteRepository.findByUsuario(usuario);
        if (optionalCliente.isPresent()) {
            return ResponseEntity.internalServerError().body("Ya existe un usuario con el nombre ingresado.");
        }

        this.clienteRepository.save(new Cliente(usuario));
        return ResponseEntity.ok().body("Cliente creado con éxito.");
    }

    public ResponseEntity<String> cargarSaldo(Long idCliente, BigDecimal saldoACargar) {
        Optional<Cliente> optionalCliente = clienteRepository.findById(idCliente);
        if (optionalCliente.isEmpty()) {
            return ResponseEntity.internalServerError().body("No existe el cliente en la base de datos.");
        }

        if (saldoACargar.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.internalServerError().body("No se puede cargar saldo con un monto menor o igual a cero.");
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

    public Cliente obtenerInfoCliente(Long idCliente) {
        return clienteRepository.findById(idCliente).orElseThrow( () -> new RuntimeException("No existe el cliente en la base de datos.") );
    }

    public ResponseEntity<Map<String, Object>> corroborarExistencia(String usuario) {
        Optional<Cliente> c = clienteRepository.findByUsuario(usuario);
        Map<String, Object> response = new HashMap<>();
        if (c.isEmpty()) {
            response.put("mensaje", "No existe un cliente con ese usuario en la base de datos.");
            return ResponseEntity.badRequest().body(response);
        }
        Cliente cliente = c.get();
        response.put("mensaje", "Hola de nuevo " + usuario + " !");
        response.put("id", cliente.getId()); // Suponiendo que Cliente tiene un campo "id"
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<String> establecerFechaDeNacimiento(Long idCliente, int yyyy, int mm, int dd) {
        Optional<Cliente> c = clienteRepository.findById(idCliente);
        if (c.isEmpty()) {
            return ResponseEntity.internalServerError().body("No existe el cliente en la base de datos.");
        }

        if (c.get().getFechaDeNacimiento() != null) {
            return ResponseEntity.internalServerError().body("No se puede cambiar la fecha de nacimiento una vez establecida.");
        }

        LocalDate fechaNacimiento = LocalDate.of(yyyy, mm, dd);

        if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18) {
            return ResponseEntity.badRequest().body("Debes ser mayor de edad para acceder al beneficio por cumpleaños.");
        }

        Cliente cliente = c.get();
        cliente.setFechaDeNacimiento(fechaNacimiento);
        clienteRepository.save(cliente);
        return ResponseEntity.ok().body("Fecha de nacimiento guardada correctamente.");
    }

    public ResponseEntity<String> obtenerPlanPrime(Long idCliente) {
        Optional<Cliente> c = clienteRepository.findById(idCliente);
        if (c.isEmpty()) {
            return ResponseEntity.internalServerError().body("No existe el cliente en la base de datos.");
        }

        Optional<Plan> p = planRepository.findByNombre("Prime");
        if (p.isEmpty()) {
            return ResponseEntity.internalServerError().body("No existe el plan Prime en la base de datos.");
        }

        BigDecimal saldoCliente = c.get().getSaldo().getMonto();
        BigDecimal precioPlanPrime = p.get().getPrecio().getMonto();

        if (saldoCliente.compareTo(precioPlanPrime) < 0) {
            return ResponseEntity.badRequest().body("No posees saldo suficiente para adquirir el plan Prime.");
        }

        // Guardamos la relación entre cliente y plan.
        Cliente cliente = c.get();
        cliente.setIdPlanPrime(p.get().getId());
        clienteRepository.save(cliente);
        return ResponseEntity.ok("El plan Prime fue adquirido con éxito.");
    }
}
