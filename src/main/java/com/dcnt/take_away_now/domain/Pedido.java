package com.dcnt.take_away_now.domain;

import com.dcnt.take_away_now.generador.GeneradorDeCodigo;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "PEDIDOS")
public class Pedido {

    public enum EstadoDelPedido {
        AGUARDANDO_PREPARACION,
        EN_PREPARACION,
        LISTO_PARA_RETIRAR,
        RETIRADO,
        CANCELADO,
        DEVOLUCION_SOLICITADA,
        DEVOLUCION_ACEPTADA,
        DEVOLUCION_DENEGADA
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_PEDIDO")
    private Long id;

    @Column(name="ESTADO")
    public Pedido.EstadoDelPedido estado = Pedido.EstadoDelPedido.AGUARDANDO_PREPARACION;

    @Column(name="PRECIO_TOTAL")
    @Convert(converter = DineroAttributeConverter.class)
    public Dinero precioTotal = new Dinero(0);

    @Column(name="FECHA_Y_HORA_ENTREGA")
    public LocalDateTime fechaYHoraDeEntrega;

    @Column(name="CODIGO")
    public String codigoDeRetiro;

    @JsonBackReference
    @OneToMany(targetEntity = ProductoPedido.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "pedido")
    private List<ProductoPedido> productosPedidos;

    @ManyToOne(targetEntity = Cliente.class)
    @JoinColumn(name = "ID_CLIENTE")
    private Cliente cliente;

    @ManyToOne(targetEntity = Negocio.class)
    @JoinColumn(name = "ID_NEGOCIO")
    private Negocio negocio;

    public Pedido(Negocio negocio, Cliente cliente) {
        this.negocio = negocio;
        this.cliente = cliente;
    }

    public void actualizarSaldosClienteYNegocio(Dinero precioTotalDelPedido, PuntosDeConfianza pdcTotalDelPedido, boolean usaPdcEnPedido) {
        // Actualizamos el saldo y los puntos de confianza del cliente, tanto si ha gastado como si ha ganado.
        if (usaPdcEnPedido) {
            if (cliente.getPuntosDeConfianza().getCantidad().compareTo(pdcTotalDelPedido.getCantidad()) < 0 ) {
                throw new RuntimeException("No tenes los pdc suficiente para realizar la compra");
            }
        }

        if (cliente.getSaldo().minus(precioTotalDelPedido).compareTo(new Dinero(0)) < 0) {
            throw new RuntimeException("No tenes el Dinero suficiente para realizar la compra");
        }

        // Si el cliente esta subscripto a un plan, se le aplica el descuento correspondiente.
        if (cliente.esPrime()) {
            Plan p = cliente.getPlan();
            precioTotalDelPedido = precioTotalDelPedido.multiply(100 - p.getDescuento()).divide(100);
        }

        cliente.setSaldo(cliente.getSaldo().minus(precioTotalDelPedido));
        cliente.setPuntosDeConfianza(cliente.getPuntosDeConfianza().minus(pdcTotalDelPedido));

        // Actualizamos el saldo del negocio.
        negocio.setSaldo(negocio.getSaldo().plus(precioTotalDelPedido));

        // Finalmente, guardamos el precio total del pedido.
        this.setPrecioTotal(precioTotalDelPedido);
    }

    public void marcarComienzoDePreparcion() {
        if (this.estado != Pedido.EstadoDelPedido.AGUARDANDO_PREPARACION) {
            throw  new RuntimeException("No se puede comenzar a preparar dicho pedido ya que el mismo no se encuentra aguardando preparación.");
        }
        this.setEstado(Pedido.EstadoDelPedido.EN_PREPARACION);
    }
    public void marcarPedidoListoParaRetirar() {
        if (this.estado != Pedido.EstadoDelPedido.EN_PREPARACION) {
            throw  new RuntimeException("No se puede marcar dicho pedido como lista para retirar ya que el mismo no se encuentra en preparación.");
        }
        this.setEstado(Pedido.EstadoDelPedido.LISTO_PARA_RETIRAR);
        this.setCodigoDeRetiro(GeneradorDeCodigo.generarCodigoAleatorio());
    }

public void confirmarRetiroDelPedido(PuntosDeConfianza pdcRecompensa, LocalDate hoy) {
        // Corroboramos que el pedido se encuentre en el estado adecuado.
        if (this.estado != Pedido.EstadoDelPedido.LISTO_PARA_RETIRAR) {
            throw  new RuntimeException("No se puede retirar dicho pedido ya que el mismo no se encuentra listo para retirar.");
        }

        // le doy al cliente sus pdc y saldo de reintegro en caso de ser necesario.
        Cliente cliente = this.getCliente();
        Dinero reintegroPorBeneficios = new Dinero(0);

        // Si el cliente esta subscripto al plan prime, se le aplica el multiplicador correspondiente.
        if (cliente.esPrime()) {
            Plan p = cliente.getPlan();
            pdcRecompensa = pdcRecompensa.multiply(p.getMultiplicadorDePuntosDeConfianza());
        }

        // Si es el cumpleaños del cliente, le damos su regalito <3
        if (cliente.esSuCumpleanios(hoy) && cliente.todaviaNoUsaBeneficioCumple(hoy)) {
            pdcRecompensa = pdcRecompensa.plus(1000);
            reintegroPorBeneficios = this.getPrecioTotal().multiply(25).divide(100);
            cliente.setFechaUltUsoBenefCumple(hoy);
        }

        cliente.setPuntosDeConfianza(cliente.getPuntosDeConfianza().plus(pdcRecompensa));
        cliente.setSaldo(cliente.getSaldo().plus(reintegroPorBeneficios));

        // Actualizamos el estado del pedido y se establece la fecha y hora de entrega.
        this.setFechaYHoraDeEntrega(LocalDateTime.now());
        this.setEstado(Pedido.EstadoDelPedido.RETIRADO);
    }

    public void cancelarPedido(PuntosDeConfianza pdcPedido) {
        // Corroboramos que el pedido se encuentre en el estado adecuado.
        List<Pedido.EstadoDelPedido> estadosPosibles = Arrays.asList(Pedido.EstadoDelPedido.AGUARDANDO_PREPARACION, Pedido.EstadoDelPedido.EN_PREPARACION, Pedido.EstadoDelPedido.LISTO_PARA_RETIRAR);
        if (!estadosPosibles.contains(this.estado)) {
            throw  new RuntimeException("No se puede cancelar dicho pedido ya que el mismo no se encuentra aguardando preparación, en preparación ni listo para retirar.");
        }

        // En función del plan del cliente y el estado del pedido se devuelven pdc y/o saldo.
        if (!cliente.esPrime()) {
            // En caso de que el estado sea AGUARDANDO_PREPARACION, entonces el cliente pierde puntos de confianza (levemente, un 5% del total que del pedido) pero recupera su dinero.
            if (this.getEstado() == Pedido.EstadoDelPedido.AGUARDANDO_PREPARACION) {
                cliente.setPuntosDeConfianza(cliente.getPuntosDeConfianza().minus(pdcPedido.multiply(0.05)));
                cliente.setSaldo(cliente.getSaldo().plus(this.getPrecioTotal()));
            }else if (this.getEstado() == Pedido.EstadoDelPedido.EN_PREPARACION) {
                //En caso de que el estado sea EN_PREPARACION, entonces el cliente pierde puntos de confianza (notablemente, un 20% del total del pedido), no recupera su dinero.
                cliente.setPuntosDeConfianza(
                        cliente.getPuntosDeConfianza().minus(pdcPedido.multiply(0.20)));
            } else if(this.getEstado() == Pedido.EstadoDelPedido.LISTO_PARA_RETIRAR) {
                //En caso de que el estado sea LISTO_PARA_RETIRAR, entonces el cliente pierde puntos de confianza (significativamente, pierde un total de 500 pdc) y no recupera su dinero.
                cliente.setPuntosDeConfianza(cliente.getPuntosDeConfianza().minus(500));
            }
        } else {
            // Al ser prime obtiene el dinero nuevamente.
            cliente.setSaldo(cliente.getSaldo().plus(this.getPrecioTotal()));
        }

        // Actualizamos el estado del pedido.
        this.setEstado(Pedido.EstadoDelPedido.CANCELADO);
    }

    public void solicitarDevolucion() {
        // Corroboramos que el pedido se encuentre en el estado adecuado.
        if (this.estado != Pedido.EstadoDelPedido.RETIRADO) {
            throw  new RuntimeException("No se puede solicitar la devolución de dicho pedido ya que el mismo no se encontraba retirado.");
        }

        // Corroboramos si el cliente tiene plan prime.
        if (!this.getCliente().esPrime()) {
            // Verificamos si pasaron menos de 5 minutos desde su retiro.
            if (Duration.between(this.getFechaYHoraDeEntrega(), LocalDateTime.now()).toMinutes() > 5) {
                throw  new RuntimeException("El tiempo de tolerancia para devolver un pedido es de cinco minutos y el mismo ya ha expirado.");
            }
        } else {
            // Verificamos si pasaron menos de 5 minutos desde su retiro.
            if (Duration.between(this.getFechaYHoraDeEntrega(), LocalDateTime.now()).toMinutes() > 15) {
                throw  new RuntimeException("El tiempo de tolerancia para devolver un pedido es de quince minutos para los beneficiarios del plan prime y el mismo ya ha expirado.");
            }
        }


        // Actualizamos el estado del pedido.
        this.setEstado(Pedido.EstadoDelPedido.DEVOLUCION_SOLICITADA);
    }

    public void aceptarDevolucion() {
        // Corroboramos que el pedido se encuentre en el estado adecuado.
        if (this.estado != Pedido.EstadoDelPedido.DEVOLUCION_SOLICITADA) {
            throw  new RuntimeException("No se puede aceptar la devolución de dicho pedido ya que el mismo no se encuentra solicitando devolución.");
        }

        // El cliente obtiene su dinero nuevamente y sus pdc no se ven afectados.
        cliente.setSaldo(cliente.getSaldo().plus(this.getPrecioTotal()));

        // Actualizamos el estado del pedido.
        this.setEstado(Pedido.EstadoDelPedido.DEVOLUCION_ACEPTADA);
    }

    public void denegarDevolucion() {
        // Corroboramos que el pedido se encuentre en el estado adecuado.
        if (this.estado != Pedido.EstadoDelPedido.DEVOLUCION_SOLICITADA) {
            throw  new RuntimeException("No se puede denegar la devolución de dicho pedido ya que el mismo no se encuentra solicitando devolución.");
        }

        // El cliente no obtiene su dinero nuevamente ni sus pdc no se ven afectados y dado que no se devuelve el pedido, el stock queda tal cual.
        // Actualizamos el estado del pedido.
        this.setEstado(Pedido.EstadoDelPedido.DEVOLUCION_DENEGADA);
    }
}
