package com.dcnt.take_away_now.domain;

import com.dcnt.take_away_now.enums.EstadoDelPedido;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "PEDIDOS")
public class Pedido {
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
}
