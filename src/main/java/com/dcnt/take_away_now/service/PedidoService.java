package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.*;
import com.dcnt.take_away_now.dto.InfoPedidoDto;
import com.dcnt.take_away_now.dto.ProductoPedidoDto;
import com.dcnt.take_away_now.enums.EstadoDelPedido;
import com.dcnt.take_away_now.generador.GeneradorDeCodigo;
import com.dcnt.take_away_now.repository.*;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import static org.springframework.http.HttpStatus.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final NegocioRepository negocioRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRegistroRepository inventarioRegistroRepository;
    private final ProductoPedidoRepository productoPedidoRepository;

    public Collection<Pedido> obtenerPedidos() {
        return pedidoRepository.findAll();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public boolean esUnProductoDeEseNegocio(Long idNegocio, Long idProducto) {
        Negocio negocio = negocioRepository.findById(idNegocio).get();
        Producto producto = productoRepository.findById(idProducto).get();
        return inventarioRegistroRepository.existsByNegocioAndProducto(negocio, producto);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public boolean sePuedeConfirmarUnPedidoParaEstosProductos(Map<Long, Integer> productos, Long idNegocio) {
        for (Map.Entry<Long, Integer> entry : productos.entrySet()) {
            if (!esUnProductoDeEseNegocio(idNegocio, entry.getKey())) {
                throw new RuntimeException("Ha ocurrido un error ya que el producto " + productoRepository.findById(entry.getKey()).get().getNombre() + " no está disponible para este negocio.");
            }

            // Levantamos la cantidad pedida por el cliente y corroboramos que exista el stock necesario.
            Integer cantidadPedida = entry.getValue();
            Producto producto = productoRepository.findById(entry.getKey()).get();
            Negocio negocio = negocioRepository.findById(idNegocio).get();
            InventarioRegistro inventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(negocio, producto).get();

            if (cantidadPedida > inventarioRegistro.getStock()) {
                throw new RuntimeException("La cantidad solicitada para el producto "+ producto.getNombre() + " es mayor al stock disponible.");
            }

        }
        return true;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public ResponseEntity<String> confirmarPedido(InfoPedidoDto dto) {
        // Dado que ya hemos corroborado todos los datos, procedemos a confirmar el pedido.
        Dinero precioTotalDelPedido = new Dinero(0);
        PuntosDeConfianza pdcTotalDelPedido = new PuntosDeConfianza((double) 0);
        Negocio negocio = negocioRepository.findById(dto.getIdNegocio()).get();
        Cliente cliente = clienteRepository.findById(dto.getIdCliente()).get();
        Pedido pedido = new Pedido(negocio, cliente);

        pedidoRepository.save(pedido);

        for (Map.Entry<Long, Integer> entry : dto.getProductos().entrySet()) {
            Integer cantidadPedida = entry.getValue();
            Producto producto = productoRepository.findById(entry.getKey()).get();

            InventarioRegistro inventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(negocio, producto).orElseThrow( () -> new RuntimeException("Ocurrió un error con el producto "+ producto.getNombre() +" al confirmar el pedido.") );

            inventarioRegistro.setStock(inventarioRegistro.getStock() - cantidadPedida);

            // Actualizamos el InventarioRegistro con el nuevo stock.
            inventarioRegistroRepository.save(inventarioRegistro);

            // Relacionamos el producto con el pedido.
            ProductoPedido productoPedido = new ProductoPedido(cantidadPedida, pedido, producto);
            productoPedidoRepository.save(productoPedido);

            // Aumentamos el precio del pedido en función de la cantidad de productos solicitados y repetimos la operación para los pdc.
            Dinero precioParcialPorProducto = new Dinero(cantidadPedida).multiply(inventarioRegistro.getPrecio());
            precioTotalDelPedido = precioTotalDelPedido.plus(precioParcialPorProducto);

            PuntosDeConfianza pdcParcialesPorProducto = inventarioRegistro.getRecompensaPuntosDeConfianza().multiply(Double.valueOf(cantidadPedida));
            pdcTotalDelPedido = pdcTotalDelPedido.plus(pdcParcialesPorProducto);
        }

        // Actualizamos el saldo y los puntos de confianza del cliente.
        cliente.setSaldo(cliente.getSaldo().minus(precioTotalDelPedido));

        if (cliente.getPuntosDeConfianza() != null) {
            cliente.setPuntosDeConfianza(cliente.getPuntosDeConfianza().plus(pdcTotalDelPedido));
        } else {
            cliente.setPuntosDeConfianza(pdcTotalDelPedido);
        }

        // Actualizamos el saldo del negocio.
        negocio.setSaldo(negocio.getSaldo().plus(precioTotalDelPedido));

        // Finalmente, guardamos el precio total del pedido.
        pedido.setPrecioTotal(precioTotalDelPedido);
        pedidoRepository.save(pedido);
        return  ResponseEntity.ok().body("El pedido fue confirmado correctamente.");
    }

    @Transactional
    public ResponseEntity<String> verificarPedido(InfoPedidoDto dto) {
        Optional<Cliente> optionalCliente = clienteRepository.findById(dto.getIdCliente());
        if (optionalCliente.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).body("No existe el cliente para el cual se busca verificar la integridad del pedido.");
        }

        Optional<Negocio> optionalNegocio = negocioRepository.findById(dto.getIdNegocio());
        if (optionalNegocio.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).body("No existe el negocio para el cual se busca verificar la integridad del pedido.");
        }

        if (!sePuedeConfirmarUnPedidoParaEstosProductos(dto.getProductos(), dto.getIdNegocio())) {
            return ResponseEntity.status(BAD_REQUEST).body("Uno o más productos presentan un stock menor a la cantidad indicada en su pedido.");
        }

        return confirmarPedido(dto);
    }


    /********************************************************
     *   Métodos referidos al cambio de estado de un pedido *
     ********************************************************/

    public ResponseEntity<String> marcarComienzoDePreparacion(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).body("No existe el pedido al cual usted quiere marcar su comienzo de preparación.");
        }

        Pedido pedido = optionalPedido.get();
        if (pedido.estado != EstadoDelPedido.AGUARDANDO_PREPARACION) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("No se puede comenzar a preparar dicho pedido ya que el mismo no se encuentra aguardando preparación.");
        }
        pedido.setEstado(EstadoDelPedido.EN_PREPARACION);
        pedidoRepository.save(pedido);
        return ResponseEntity.status(ACCEPTED).body("Se ha marcado que el pedido está en comienzo de preparación.");
    }

    public ResponseEntity<String> marcarPedidoListoParaRetirar(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).body("No existe el pedido al cual usted quiere marcar como disponible su retiro.");
        }

        Pedido pedido = optionalPedido.get();
        if (pedido.estado != EstadoDelPedido.EN_PREPARACION) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("No se puede marcar dicho pedido como lista para retirar ya que el mismo no se encuentra en preparación.");
        }
        pedido.setEstado(EstadoDelPedido.LISTO_PARA_RETIRAR);
        pedido.setCodigoDeRetiro(GeneradorDeCodigo.generarCodigoAleatorio());
        pedidoRepository.save(pedido);
        return ResponseEntity.status(ACCEPTED).body("Se ha marcado que el pedido está listo para retirar.");
    }

    public ResponseEntity<String> confirmarRetiroDelPedido(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).body("No existe el pedido al cual usted quiere confirmar el retiro.");
        }

        Pedido pedido = optionalPedido.get();
        if (pedido.estado != EstadoDelPedido.LISTO_PARA_RETIRAR) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("No se puede retirar dicho pedido ya que el mismo no se encuentra listo para retirar.");
        }
        // Actualizamos el estado del pedido y se establece la fecha y hora de entrega.
        pedido.setFechaYHoraDeEntrega(LocalDateTime.now());
        pedido.setEstado(EstadoDelPedido.RETIRADO);
        pedidoRepository.save(pedido);
        return ResponseEntity.status(ACCEPTED).body("Se ha confirmado el retiro del pedido.");
    }

    public ResponseEntity<String> cancelarPedido(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).body("No existe el pedido que usted busca cancelar.");
        }

        Pedido pedido = optionalPedido.get();
        List<EstadoDelPedido> estadosPosibles = Arrays.asList(EstadoDelPedido.AGUARDANDO_PREPARACION, EstadoDelPedido.EN_PREPARACION, EstadoDelPedido.LISTO_PARA_RETIRAR);
        if (!estadosPosibles.contains(pedido.estado)) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("No se puede cancelar dicho pedido ya que el mismo no se encuentra aguardando preparación, en preparación ni listo para retirar.");
        }

        Cliente c = clienteRepository.findById(pedido.getCliente().getId()).orElseThrow( () -> new RuntimeException("Ocurrió un error al obtener los datos del cliente."));

        // En caso de que el estado sea AGUARDANDO_PREPARACION, entonces el cliente pierde puntos de confianza (levemente, un 5% del total que posee) pero recupera su dinero.
        if (pedido.getEstado() == EstadoDelPedido.AGUARDANDO_PREPARACION) {
            c.setPuntosDeConfianza(c.getPuntosDeConfianza().minus(c.getPuntosDeConfianza().multiply(0.05)));
            c.setSaldo(c.getSaldo().plus(pedido.getPrecioTotal()));
        } else if (pedido.getEstado() == EstadoDelPedido.EN_PREPARACION) {
            //En caso de que el estado sea EN_PREPARACION, entonces el cliente pierde puntos de confianza (notablemente, un 20% del total que posee), no recupera su dinero.
            c.setPuntosDeConfianza(
                    c.getPuntosDeConfianza().minus(c.getPuntosDeConfianza().multiply(0.20)));
        } else if(pedido.getEstado() == EstadoDelPedido.LISTO_PARA_RETIRAR) {
            //En caso de que el estado sea LISTO_PARA_RETIRAR, entonces el cliente pierde puntos de confianza (significativamente, pierde un 100% del total que posee), no recupera su dinero.
            c.setPuntosDeConfianza(new PuntosDeConfianza((double) 0));
        }

        clienteRepository.save(c);

        // Se actualiza el stock de cada producto
        devolverStockDeUnPedido(pedido);

        // Actualizamos el estado del pedido.
        pedido.setEstado(EstadoDelPedido.CANCELADO);
        pedidoRepository.save(pedido);
        return ResponseEntity.status(ACCEPTED).body("Se ha cancelado el pedido.");
    }

    public ResponseEntity<String> devolverPedido(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).body("No existe el pedido que usted busca devolver.");
        }

        Pedido pedido = optionalPedido.get();
        if (pedido.estado != EstadoDelPedido.RETIRADO) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("No se puede devolver dicho pedido ya que el mismo no se encontraba retirado.");
        }

        // Verificamos si pasaron menos de 5 minutos desde su entrega.
        if (Duration.between(pedido.getFechaYHoraDeEntrega(), LocalDateTime.now()).toMinutes() > 5) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("El tiempo de tolerancia para devolver un pedido es de cinco minutos y el mismo ya ha expirado.");
        }

        // El cliente obtiene su dinero nuevamente.
        Cliente c = clienteRepository.findById(pedido.getCliente().getId()).orElseThrow( () -> new RuntimeException("Ocurrió un error al reintegrar el total del pedido al cliente."));
        c.setSaldo(c.getSaldo().plus(pedido.getPrecioTotal()));

        // Se actualiza el stock de cada producto
        devolverStockDeUnPedido(pedido);

        // Actualizamos el estado del pedido.
        pedido.setEstado(EstadoDelPedido.DEVUELTO);
        pedidoRepository.save(pedido);
        return ResponseEntity.status(ACCEPTED).body("Se ha confirmado la devolución del pedido.");
    }

    private void devolverStockDeUnPedido(Pedido pedido) {
        Collection< ProductoPedidoDto> productoPedidoDtos = productoPedidoRepository.obtenerProductosDelPedido(pedido.getId());

        //Se actualiza el stock de cada producto
        for (ProductoPedidoDto entry : productoPedidoDtos) {
            Integer cantidadPedida = entry.getCantidad();
            Producto p = entry.getProducto();
            InventarioRegistro inventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(pedido.getNegocio(), p).orElseThrow( () -> new RuntimeException("Ocurrió un error con el producto "+ entry.getProducto().getNombre() +" al confirmar el pedido.") );

            inventarioRegistro.setStock(inventarioRegistro.getStock() + cantidadPedida);

            // Actualizamos el InventarioRegistro con el nuevo stock.
            inventarioRegistroRepository.save(inventarioRegistro);
        }
    }

    public Collection<ProductoPedidoDto> obtenerDetalleDelPedido(Long idPedido) {
        return productoPedidoRepository.obtenerProductosDelPedido(idPedido);
    }
}
