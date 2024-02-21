package com.dcnt.take_away_now.domain;

import com.dcnt.take_away_now.enums.EstadoDelPedido;
import com.dcnt.take_away_now.generador.GeneradorDeCodigo;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public EstadoDelPedido estado = EstadoDelPedido.AGUARDANDO_PREPARACION;

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

    public void actualizarSaldosClienteYNegocio(Dinero precioTotalDelPedido, PuntosDeConfianza pdcTotalDelPedido) {
        // Actualizamos el saldo y los puntos de confianza del cliente, tanto si ha gastado como si ha ganado.
        if (cliente.getSaldo().minus(precioTotalDelPedido).compareTo(new Dinero(0)) < 0) {
            throw new RuntimeException("No tenes el Dinero suficiente para realizar la compra");
        }

        // Si el cliente esta subscripto a un plan, se le aplica el descuento correspondiente.
        if (cliente.esPrime()) {
            Plan p = cliente.getPlan();
            precioTotalDelPedido = precioTotalDelPedido.multiply(p.getDescuento()).divide(100);
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

    public void confirmarRetiroDelPedido(PuntosDeConfianza pdcRecompensa) {
        // le doy al cliente sus pdc y saldo de reintegro en caso de ser necesario.
        Cliente cliente = this.getCliente();
        Dinero reintegroPorBeneficios = new Dinero(0);

        // Si el cliente esta subscripto al plan prime, se le aplica el multiplicador correspondiente.
        if (cliente.esPrime()) {
            Plan p = cliente.getPlan();
            pdcRecompensa = pdcRecompensa.multiply(p.getMultiplicadorDePuntosDeConfianza());
        }

        // Si es el cumpleaños del cliente, le damos su regalito <3
        if (cliente.esSuCumpleanios() && cliente.todaviaNoUsaBeneficioCumple()) {
            pdcRecompensa = pdcRecompensa.plus(1000);
            reintegroPorBeneficios = this.getPrecioTotal().multiply(25).divide(100);
            cliente.setFechaUltUsoBenefCumple(LocalDate.now());
        }

        cliente.setPuntosDeConfianza(cliente.getPuntosDeConfianza().plus(pdcRecompensa));
        cliente.setSaldo(cliente.getSaldo().plus(reintegroPorBeneficios));

        // Actualizamos el estado del pedido y se establece la fecha y hora de entrega.
        this.setFechaYHoraDeEntrega(LocalDateTime.now());
        this.setEstado(Pedido.EstadoDelPedido.RETIRADO);
    }

    public void cancelarPedido(PuntosDeConfianza pdcPedido) {
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
                //En caso de que el estado sea LISTO_PARA_RETIRAR, entonces el cliente pierde puntos de confianza (significativamente, pierde un 100% del total que posee o en caso de tener pdc negativos, adeudará 500 pdc adicionales) y no recupera su dinero.
                if (cliente.getPuntosDeConfianza().getCantidad() <= 0) {
                    cliente.setPuntosDeConfianza(cliente.getPuntosDeConfianza().minus(500));
                } else {
                    cliente.setPuntosDeConfianza(new PuntosDeConfianza((double) 0));
                }
            }
        } else {
            // Al ser prime obtiene el dinero nuevamente.
            cliente.setSaldo(cliente.getSaldo().plus(this.getPrecioTotal()));
        }

        // Actualizamos el estado del pedido.
        this.setEstado(Pedido.EstadoDelPedido.CANCELADO);
    }

    public void solicitarDevolucion() {
        // Corroboramos si el cliente tiene plan prime.
        if (this.getCliente().esPrime()) {
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
        // El cliente obtiene su dinero nuevamente y sus pdc no se ven afectados.
        cliente.setSaldo(cliente.getSaldo().plus(this.getPrecioTotal()));

        // Actualizamos el estado del pedido.
        this.setEstado(Pedido.EstadoDelPedido.DEVOLUCION_ACEPTADA);
    }

    public void denegarDevolucion() {
        // El cliente no obtiene su dinero nuevamente ni sus pdc no se ven afectados y dado que no se devuelve el pedido, el stock queda tal cual.
        // Actualizamos el estado del pedido.
        this.setEstado(Pedido.EstadoDelPedido.DEVOLUCION_DENEGADA);
    }
}
