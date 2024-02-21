package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.Cliente;
import com.dcnt.take_away_now.domain.Plan;
import com.dcnt.take_away_now.dto.PedidoDto;
import com.dcnt.take_away_now.repository.ClienteRepository;
import com.dcnt.take_away_now.repository.PedidoRepository;
import com.dcnt.take_away_now.repository.PlanRepository;
import com.dcnt.take_away_now.repository.ProductoPedidoRepository;
import com.dcnt.take_away_now.value_object.Dinero;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Optional;

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
    public void crearCliente(String usuario) {
        Optional<Cliente> optionalCliente = clienteRepository.findByUsuario(usuario);
        if (optionalCliente.isPresent()) {
            throw new RuntimeException("Ya existe un usuario con el nombre ingresado.");
        }

        this.clienteRepository.save(new Cliente(usuario));
    }

    public void cargarSaldo(Long idCliente, BigDecimal saldoACargar) {
        Optional<Cliente> optionalCliente = clienteRepository.findById(idCliente);
        if (optionalCliente.isEmpty()) {
            throw new RuntimeException("No existe el cliente en la base de datos.");
        }

        if (saldoACargar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("No se puede cargar saldo con un monto menor o igual a cero.");
        }

        Cliente cliente = optionalCliente.get();
        cliente.setSaldo(cliente.getSaldo().plus(new Dinero(saldoACargar)));
        this.clienteRepository.save(cliente);
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

    public Long corroborarExistencia(String usuario) {
        Optional<Cliente> c = clienteRepository.findByUsuario(usuario);
        if (c.isEmpty()) {
            return (long) -1;
        }
        Cliente cliente = c.get();
        return cliente.getId();
    }

    public void establecerFechaDeNacimiento(Long idCliente, int yyyy, int mm, int dd) {
        Optional<Cliente> c = clienteRepository.findById(idCliente);
        if (c.isEmpty()) {
            throw new RuntimeException("No existe el cliente en la base de datos.");
        }

        if (c.get().getFechaDeNacimiento() != null) {
            throw new RuntimeException("No se puede cambiar la fecha de nacimiento una vez establecida.");
        }

        LocalDate fechaNacimiento = LocalDate.of(yyyy, mm, dd);

        if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18) {
            throw new RuntimeException("Debes ser mayor de edad para acceder al beneficio por cumpleaños.");
        }

        Cliente cliente = c.get();
        cliente.setFechaDeNacimiento(fechaNacimiento);
        clienteRepository.save(cliente);
    }

    public void obtenerPlanPrime(Long idCliente) {
        Optional<Cliente> c = clienteRepository.findById(idCliente);
        if (c.isEmpty()) {
            throw new RuntimeException("No existe el cliente en la base de datos.");
        }

        Optional<Plan> p = planRepository.findByNombre("Prime");
        if (p.isEmpty()) {
            throw new RuntimeException("No existe el plan Prime en la base de datos.");
        }

        BigDecimal saldoCliente = c.get().getSaldo().getMonto();
        BigDecimal precioPlanPrime = p.get().getPrecio().getMonto();

        if (c.get().esPrime()) {
            throw new RuntimeException("Ya estás suscripto al plan Prime.");
        }

        if (saldoCliente.compareTo(precioPlanPrime) < 0) {
            throw new RuntimeException("No posees saldo suficiente para adquirir el plan Prime.");
        }

        // Guardamos la relación entre cliente y plan.
        Cliente cliente = c.get();
        cliente.setIdPlanPrime(p.get().getId());
        clienteRepository.save(cliente);
    }
}
