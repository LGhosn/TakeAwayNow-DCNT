package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.*;
import com.dcnt.take_away_now.dto.InfoPedidoDto;
import com.dcnt.take_away_now.dto.ProductoPedidoDto;
import com.dcnt.take_away_now.generador.GeneradorDeCodigo;
import com.dcnt.take_away_now.repository.*;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import static org.springframework.http.HttpStatus.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
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
    private final PlanRepository planRepository;

    public Collection<Pedido> obtenerPedidos() {
        return pedidoRepository.findAll();
    }


    public boolean esUnProductoDeEseNegocio(Long idNegocio, Long idProducto) {
        if (negocioRepository.findById(idNegocio).isEmpty()) {
            ResponseEntity.status(NOT_FOUND).body("No existe el negocio en la base de datos.");
        }

        if (productoRepository.findById(idProducto).isEmpty()) {
            ResponseEntity.status(NOT_FOUND).body("No existe el producto en la base de datos.");
        }

        Negocio negocio = negocioRepository.findById(idNegocio).get();
        Producto producto = productoRepository.findById(idProducto).get();

        return inventarioRegistroRepository.existsByNegocioAndProducto(negocio, producto);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    /* Queda con esta annotation ya que previo al get se valida la existencia del registro en la base */
    public boolean sePuedeConfirmarUnPedidoParaEstosProductos(Map<Long, Map<String, Object>> productos, Long idNegocio) {
        for (Map.Entry<Long, Map<String, Object>> entry : productos.entrySet()) {
            Long productId = entry.getKey();
            Map<String, Object> productInfo = entry.getValue();

            // Obtenemos la cantidad  pedida y si usa o no PdC
            Integer cantidadPedida = (Integer) productInfo.get("cantidad");

            if (!esUnProductoDeEseNegocio(idNegocio, productId)) {
                Producto producto = productoRepository.findById(productId).get();
                throw new RuntimeException("Ha ocurrido un error ya que el producto " + producto.getNombre() + " no está disponible para este negocio.");
            }

            Producto producto = productoRepository.findById(productId).get();
            Negocio negocio = negocioRepository.findById(idNegocio).get();
            InventarioRegistro inventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(negocio, producto).get();

            if (cantidadPedida > inventarioRegistro.getStock()) {
                throw new RuntimeException("La cantidad solicitada para el producto "+ producto.getNombre() + " es mayor al stock disponible.");
            }
        }
        return true;
    }

    public boolean elClienteTieneSaldoSuficiente(Long idCliente, Map<Long, Map<String, Object>> productos) {
        Dinero precioTotalDelPedido = new Dinero(0);
        PuntosDeConfianza pdcTotalDelPedido = new PuntosDeConfianza(0);

        for (Map.Entry<Long, Map<String, Object>> entry : productos.entrySet()) {
            Long productId = entry.getKey();
            Map<String, Object> productInfo = entry.getValue();

            // Obtenemos la cantidad  pedida y si usa o no PdC
            Integer cantidadPedida = (Integer) productInfo.get("cantidad");
            Integer usaPdc = (Integer) productInfo.get("usaPdc");

            // Levantamos los datos necesarios
            if (productoRepository.findById(productId).isEmpty()) {
                ResponseEntity.status(NOT_FOUND).body("No existe el producto en la base de datos.");
            }

            if (clienteRepository.findById(idCliente).isEmpty()) {
                ResponseEntity.status(NOT_FOUND).body("No existe el cliente en la base de datos.");
            }

            Producto producto = productoRepository.findById(productId).get();
            Cliente cliente = clienteRepository.findById(idCliente).get();

            // En caso de usar pdc para el producto iterado actualmente, sumamos a los pdc del pedido.
            if (usaPdc == 1) {
                pdcTotalDelPedido.plus(producto.getInventarioRegistro().getPrecioPDC().multiply(cantidadPedida));
            } else {
                precioTotalDelPedido.plus(producto.getInventarioRegistro().getPrecio().multiply(cantidadPedida));
            }

            if (!cliente.tieneSaldoSuficiente(precioTotalDelPedido, pdcTotalDelPedido, usaPdc == 1))
                return false;

        }
        return true;
    }

    public void confirmarPedido(InfoPedidoDto dto) {
        // Dado que ya hemos corroborado todos los datos, procedemos a confirmar el pedido.

        // Inicializamos las variables a utilizar para la resta de monto, pdc y la recompensa por la compra.
        Dinero precioTotalDelPedido = new Dinero(0);
        PuntosDeConfianza pdcTotalDelPedido = new PuntosDeConfianza((double) 0);
        boolean usaPdcEnPedido = false;

        // Levantamos los datos necesarios
        if (negocioRepository.findById(dto.getIdNegocio()).isEmpty()) {
            ResponseEntity.status(NOT_FOUND).body("No existe el negocio en la base de datos.");
        }

        if (clienteRepository.findById(dto.getIdCliente()).isPresent()) {
            ResponseEntity.status(NOT_FOUND).body("No existe el cliente en la base de datos.");
        }

        Negocio negocio = negocioRepository.findById(dto.getIdNegocio()).get();
        Cliente cliente = clienteRepository.findById(dto.getIdCliente()).get();

        Pedido pedido = new Pedido(negocio, cliente);

        pedidoRepository.save(pedido);

        PuntosDeConfianza pdcCliente = cliente.getPuntosDeConfianza();
        Boolean usoTodosSusPdc = false;
        for (Map.Entry<Long, Map<String, Object>> entry : dto.getProductos().entrySet()) {

            Long productId = entry.getKey();
            Map<String, Object> productInfo = entry.getValue();

            Producto producto = productoRepository.findById(productId).get();
            Integer cantidadPedida = (Integer) productInfo.get("cantidad");
            Integer usaPdc = (Integer) productInfo.get("usaPdc");

            // Actualizamos el InventarioRegistro con el nuevo stock.
            InventarioRegistro inventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(negocio, producto).orElseThrow( () -> new RuntimeException("Ocurrió un error con el producto "+ producto.getNombre() +" al confirmar el pedido.") );
            inventarioRegistro.setStock(inventarioRegistro.getStock() - cantidadPedida);
            inventarioRegistroRepository.save(inventarioRegistro);

            // Relacionamos el producto con el pedido.
            ProductoPedido productoPedido = new ProductoPedido(cantidadPedida, pedido, producto);
            productoPedidoRepository.save(productoPedido);

            // En caso de usar pdc lo tenemos en cuenta a la hora de restar saldo y cantidad pdc.
            usaPdcEnPedido = usaPdc == 1;

            // Aumentamos el precio del pedido en función de la cantidad de productos solicitados y repetimos la operación para los pdc.
            if (usaPdc == 1 && !usoTodosSusPdc) {
                PuntosDeConfianza pdcParcialesPorProducto = inventarioRegistro.getPrecioPDC().multiply(Double.valueOf(cantidadPedida));
                if (pdcCliente.getCantidad() < pdcParcialesPorProducto.getCantidad()) {
                    pdcTotalDelPedido = pdcCliente;
                    usoTodosSusPdc = true;
                    double porcentajeFaltante = pdcCliente.getCantidad() / pdcParcialesPorProducto.getCantidad();
                    Dinero precioParcialPorProducto = new Dinero(inventarioRegistro.getPrecio().getMonto()).multiply(new Dinero(BigDecimal.valueOf(porcentajeFaltante))).multiply(cantidadPedida);
                    precioTotalDelPedido = precioTotalDelPedido.plus(precioParcialPorProducto);
                    continue;
                }
                pdcTotalDelPedido = pdcTotalDelPedido.plus(pdcParcialesPorProducto);
            } else {
                Dinero precioParcialPorProducto = new Dinero(inventarioRegistro.getPrecio().getMonto()).multiply(cantidadPedida);
                precioTotalDelPedido = precioTotalDelPedido.plus(precioParcialPorProducto);
            }
        }
        pedido.actualizarSaldosClienteYNegocio(precioTotalDelPedido, pdcTotalDelPedido, usaPdcEnPedido);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void verificarPedido(InfoPedidoDto dto) {
        Optional<Cliente> optionalCliente = clienteRepository.findById(dto.getIdCliente());
        if (optionalCliente.isEmpty()) {
            throw  new RuntimeException("No existe el cliente para el cual se busca verificar la integridad del pedido.");
        }

        Optional<Negocio> optionalNegocio = negocioRepository.findById(dto.getIdNegocio());
        if (optionalNegocio.isEmpty()) {
            throw  new RuntimeException("No existe el negocio para el cual se busca verificar la integridad del pedido.");
        }

        if (!sePuedeConfirmarUnPedidoParaEstosProductos(dto.getProductos(), dto.getIdNegocio())) {
            throw  new RuntimeException("Uno o más productos presentan un stock menor a la cantidad indicada en su pedido.");
        }

        if (!elClienteTieneSaldoSuficiente(dto.getIdCliente(), dto.getProductos())) {
            throw  new RuntimeException("No posees saldo suficiente para confirmar este pedido.");
        }

        confirmarPedido(dto);
    }


    /********************************************************
     *   Métodos referidos al cambio de estado de un pedido *
     ********************************************************/

    public void marcarComienzoDePreparacion(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            throw  new RuntimeException("No existe el pedido al cual usted quiere marcar su comienzo de preparación.");
        }

        Pedido pedido = optionalPedido.get();
        pedido.marcarComienzoDePreparcion();
        pedidoRepository.save(pedido);
    }

    public void marcarPedidoListoParaRetirar(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            throw  new RuntimeException("No existe el pedido al cual usted quiere marcar como disponible su retiro.");
        }

        Pedido pedido = optionalPedido.get();
        pedido.marcarPedidoListoParaRetirar();
        pedidoRepository.save(pedido);
    }

    public void confirmarRetiroDelPedido(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            throw  new RuntimeException("No existe el pedido al cual usted quiere confirmar el retiro.");
        }

        Pedido pedido = optionalPedido.get();
        if (pedido.estado != Pedido.EstadoDelPedido.LISTO_PARA_RETIRAR) {
            throw  new RuntimeException("No se puede retirar dicho pedido ya que el mismo no se encuentra listo para retirar.");
        }
        PuntosDeConfianza pdcRecompensa =  obtenerPuntosDeConfianzaDeUnPedido(idPedido);
        pedido.confirmarRetiroDelPedido(pdcRecompensa, LocalDate.now());
        pedidoRepository.save(pedido);
    }

    public void cancelarPedido(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            throw  new RuntimeException("No existe el pedido que usted busca cancelar.");
        }

        Pedido pedido = optionalPedido.get();
        List<Pedido.EstadoDelPedido> estadosPosibles = Arrays.asList(Pedido.EstadoDelPedido.AGUARDANDO_PREPARACION, Pedido.EstadoDelPedido.EN_PREPARACION, Pedido.EstadoDelPedido.LISTO_PARA_RETIRAR);
        if (!estadosPosibles.contains(pedido.estado)) {
            throw  new RuntimeException("No se puede cancelar dicho pedido ya que el mismo no se encuentra aguardando preparación, en preparación ni listo para retirar.");
        }

        PuntosDeConfianza pdcPedido = obtenerPuntosDeConfianzaDeUnPedido(idPedido);
        Cliente c = pedido.getCliente();
        pedido.cancelarPedido(pdcPedido);

        clienteRepository.save(c);

        // Se actualiza el stock de cada producto
        devolverStockDeUnPedido(pedido);

        pedidoRepository.save(pedido);
    }

    public void solicitarDevolucion(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            throw  new RuntimeException("No existe el pedido que usted busca solicitar su devolución.");
        }

        Pedido pedido = optionalPedido.get();
        if (pedido.estado != Pedido.EstadoDelPedido.RETIRADO) {
            throw  new RuntimeException("No se puede solicitar la devolución de dicho pedido ya que el mismo no se encontraba retirado.");
        }

        pedido.solicitarDevolucion();
        pedidoRepository.save(pedido);
    }

    public void aceptarDevolucion(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            throw  new RuntimeException("No existe el pedido al cual usted busca aceptar su devolución.");
        }

        Pedido pedido = optionalPedido.get();
        if (pedido.estado != Pedido.EstadoDelPedido.DEVOLUCION_SOLICITADA) {
            throw  new RuntimeException("No se puede aceptar la devolución de dicho pedido ya que el mismo no se encuentra solicitando devolución.");
        }

        pedido.aceptarDevolucion();

        // Se actualiza el stock de cada producto
        devolverStockDeUnPedido(pedido);

        pedidoRepository.save(pedido);
    }

    public void denegarDevolucion(Long idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty()) {
            throw  new RuntimeException("No existe el pedido al cual usted busca denegar su devolución.");
        }

        Pedido pedido = optionalPedido.get();
        if (pedido.estado != Pedido.EstadoDelPedido.DEVOLUCION_SOLICITADA) {
            throw  new RuntimeException("No se puede denegar la devolución de dicho pedido ya que el mismo no se encuentra solicitando devolución.");
        }

        pedido.denegarDevolucion();
        pedidoRepository.save(pedido);
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

    private PuntosDeConfianza obtenerPuntosDeConfianzaDeUnPedido(Long idPedido) {
        Collection< ProductoPedidoDto> productoPedidoDtos = productoPedidoRepository.obtenerProductosDelPedido(idPedido);
        Pedido pedido = pedidoRepository.findById(idPedido).get();

        PuntosDeConfianza pdcRecompensa = new PuntosDeConfianza(0);
        for (ProductoPedidoDto entry : productoPedidoDtos) {
            Producto p = entry.getProducto();
            InventarioRegistro inventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(pedido.getNegocio(), p).orElseThrow( () -> new RuntimeException("Ocurrió un error con el producto "+ entry.getProducto().getNombre() +" al confirmar el pedido.") );

            pdcRecompensa = pdcRecompensa.plus(inventarioRegistro.getRecompensaPuntosDeConfianza().getCantidad() * entry.getCantidad());
        }
        return pdcRecompensa;
    }
}
