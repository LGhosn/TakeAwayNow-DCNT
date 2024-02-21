package com.dcnt.take_away_now.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;

import java.time.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "NEGOCIOS")
public class Negocio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_NEGOCIO")
    private Long id;

    @Column(name="NOMBRE")
    public String nombre;

    @Column(name="SALDO")
    @Convert(converter = DineroAttributeConverter.class)
    public Dinero saldo;

    @Column(name="DIA_APERTURA")
    public DayOfWeek diaDeApertura;

    @Column(name="DIA_CIERRE")
    public DayOfWeek diaDeCierre;

    @Column(name="HORARIO_APERTURA")
    public LocalTime horarioDeApertura;

    @Column(name="HORARIO_CIERRE")
    public LocalTime horarioDeCierre;

    @JsonBackReference
    @OneToMany(targetEntity = InventarioRegistro.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "negocio")
    private List<InventarioRegistro> inventarioRegistros;

    @JsonBackReference
    @OneToMany(targetEntity = Pedido.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "negocio")
    private List<Pedido> pedidos;

    /**
     *
     * Crea un negocio. Si el horario de apertura es mayor al de cierre se lanza un error.
     *
     */
    public Negocio(
            String nombreDelNegocio,
            LocalTime horarioDeApertura,
            LocalTime horarioDeCierre,
            DayOfWeek diaDeApertura,
            DayOfWeek diaDeCierre
    ) {
        this.nombre = nombreDelNegocio;
        this.horarioDeApertura = horarioDeApertura;
        this.horarioDeCierre = horarioDeCierre;
        this.diaDeApertura = diaDeApertura;
        this.diaDeCierre = diaDeCierre;
        this.saldo = new Dinero(0);
    }

    /**
     *
     * Indica si el negocio está cerrado según el horario.
     *
     */
    public boolean estaCerrado(LocalDateTime FechaYHora) {
        return !estaAbierto(FechaYHora);
    }

    /**
     *
     * Indica si el negocio esta abierto según el horario.
     *
     */
    public boolean estaAbierto(LocalDateTime FechaYHora) {
        // Obtener la zona horaria de Buenos Aires, y posteriormente la utilizamos para comparar los horarios.
        ZoneId zonaHorariaArgentina = ZoneId.of("America/Argentina/Buenos_Aires");
        ZonedDateTime fechaYHoraArgentina = FechaYHora.atZone(zonaHorariaArgentina);

        // Obtenemos el horario y el día en Argentina.
        LocalTime horarioIngresado = fechaYHoraArgentina.toLocalTime();
        DayOfWeek diaIngresado = fechaYHoraArgentina.getDayOfWeek();

        // Resto del código sigue igual
        return (
                ((diaIngresado.compareTo(diaDeApertura) >= 0) && (diaIngresado.compareTo(diaDeCierre) <= 0))
                        && (horarioIngresado.isAfter(horarioDeApertura) || horarioIngresado.equals(horarioDeApertura))
                        && (horarioIngresado.isBefore(horarioDeCierre) || horarioIngresado.equals(horarioDeCierre))
        );
    }

    public void registrarProductoEnInventario(InventarioRegistro inventarioRegistro) {
        if (inventarioRegistros != null) {
            inventarioRegistros.add(inventarioRegistro);
        }
    }

    /*
     *
     * Registra el producto recibido en el almacén. Si la cantidad del producto recibido es 0 entonces
     * se lanza un error.
     *

    void registrarProducto(Producto producto) {
        if (producto.cantidad == 0) throw new IllegalStateException("No se puede registrar un producto sin stock.")
        this.almacén.agregar(producto)
    }
     */

    /*
     *
     * Aumenta el stock del producto con el nombre recibido. El nuevo stock sera al actual aumentado el recibido.
     * Si el stock es menor o igual a cer entonces se lanza un error.
     *

    void ingresarStock(String nombreDelProducto, int nuevoStock) {
        if (nuevoStock <= 0) throw new IllegalStateException("No se puede ingresar un stock menor o igual a cero.")
        this.almacen.actualizarStock(nombreDelProducto, nuevoStock)
    }*/

    /*
     *
     * Verifica si hay stock del producto con el nombre recibido.
     *

    boolean hayStock(String nombreDelProducto) {
        this.almacen.hayStock(nombreDelProducto)
    }*/

    /*
     *
     * Agrega la cantidad indicada del producto con el nombre recibido al pedido. En caso
     * de que no se pueda agregar al pedido se devolvera false.
     *

    boolean agregarAlPedido(String nombreProducto, int cantidad, Pedido pedido) {
        this.almacen.retirarProducto(nombreProducto, cantidad, pedido)
    }*/


    /*
     *
     * Agrega un producto al pedido indicado a cambio de puntos de confianza. Si no es
     * posible agregar el producto al pedido se retorna false.
     *

    boolean agregarAlPedidoPorPuntoDeConfianza(String nombreProducto, int cantidad, Pedido pedido) {
        this.almacen.retirarProductoPorPuntosDeConfianza(nombreProducto, cantidad, pedido)
    }*/

    /*
     *
     * TODO
     *

    void reingresarStockDelPedido(int id) {
        Set<Producto> productos = this.comprasRegistradas[id].getPedido().getProductos()
        productos.each{ producto -> this.ingresarStock(producto.getNombre(), producto.getCantidad()) }
    }*/


    /* MÉTODOS REFERIDOS A LA ETAPA DE REGISTRO Y RETIRO DE COMPRAS */

    /**
     *
     * TODO
     *

    Compra registrarCompra(Pedido pedidoConfirmado) {
        int idCompraRegistrada = ids_compras++
        Compra compraRegistrada = new Compra(pedidoConfirmado, idCompraRegistrada)
        this.comprasRegistradas[idCompraRegistrada] = compraRegistrada
        compraRegistrada
    }*/

    /**
     *
     * TODO
     *

    void prepararCompra(int id) {
        if (!comprasRegistradas[id]) throw new Exception("No se encuentra registrada una compra con el ID indicado.")
        if (comprasRegistradas[id].estado != Compra.EstadoDeCompra.AGUARDANDO_PREPARACION) throw new Exception("No se puede preparar dicha compra ya que la misma no se encontraba AGUARDANDO_PREPARACION.")
        comprasRegistradas[id].estado = Compra.EstadoDeCompra.EN_PREPARACION
    }*/

    /**
     *
     * TODO
     *

    void marcarCompraListaParaRetirar(int id) {
        if (!comprasRegistradas[id]) throw new Exception("No se encuentra registrada una compra con el ID indicado.")
        if (comprasRegistradas[id].estado != Compra.EstadoDeCompra.EN_PREPARACION) throw new Exception("No se puede marcar dicha compra como LISTA_PARA_RETIRAR ya que la misma no se encontraba EN_PREPARACION.")
        comprasRegistradas[id].estado = Compra.EstadoDeCompra.LISTA_PARA_RETIRAR
    }*/

    /**
     *
     * TODO
     *

    void marcarCompraRetirada(int id) {
        if (!comprasRegistradas[id]) throw new Exception("No se encuentra registrada una compra con el ID indicado.")
        if (comprasRegistradas[id].estado != Compra.EstadoDeCompra.LISTA_PARA_RETIRAR) throw new Exception("No se puede marcar dicha compra como RETIRADA ya que la misma no se encontraba LISTA_PARA_RETIRAR.")
        comprasRegistradas[id].estado = Compra.EstadoDeCompra.RETIRADA
    }*/

    /**
     *
     * TODO
     *

    void marcarCompraCancelada(int id) {
        if (!comprasRegistradas[id]) throw new Exception("No se encuentra registrada una compra con el ID indicado.")
        if (comprasRegistradas[id].estado != Compra.EstadoDeCompra.RETIRADA) throw new Exception("No se puede marcar dicha compra como CANCELADA ya que la misma no se encontraba RETIRADA.")
        comprasRegistradas[id].estado = Compra.EstadoDeCompra.CANCELADA
    }*/

    /**
     *
     * TODO
     *

    void marcarCompraDevuelta(int id) {
        if (!comprasRegistradas[id]) throw new Exception("No se encuentra registrada una compra con el ID indicado.")
        if (comprasRegistradas[id].estado != Compra.EstadoDeCompra.RETIRADA) throw new Exception("No se puede marcar dicha compra como DEVUELTA ya que la misma no se encontraba RETIRADA.")
        comprasRegistradas[id].estado = Compra.EstadoDeCompra.DEVUELTA
    }*/

    /**
     *
     * TODO
     *

    Compra.EstadoDeCompra estadoDeCompra(int id) {
        if (!comprasRegistradas[id]) throw new Exception("No se encuentra registrada una compra con el ID indicado.")
        comprasRegistradas[id].getEstado()
    }*/

    /**
     *
     * TODO
     *

    void devolucionDelPedido(int id) {
        if (!comprasRegistradas[id]) throw new Exception("No se encuentra registrada una compra con el ID indicado.")
        if (comprasRegistradas[id].estado != Compra.EstadoDeCompra.RETIRADA) throw new Exception("No se puede devolver el pedido ya que la compra no se encontraba RETIRADA.")
        this.reingresarStockDelPedido(id)
        this.marcarCompraDevuelta(id)
    }*/
}
