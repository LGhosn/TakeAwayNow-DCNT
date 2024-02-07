package com.dcnt.take_away_now.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PRODUCTOS_PEDIDOS")
public class ProductoPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRODUCTO_PEDIDO")
    private Long id;

    @Column(name="CANTIDAD")
    private Integer cantidad;

    @ManyToOne(targetEntity = Pedido.class)
    @JoinColumn(name = "ID_PEDIDO")
    private Pedido pedido;

    @ManyToOne(targetEntity = Producto.class)
    @JoinColumn(name = "ID_PRODUCTO")
    private Producto producto;

    public ProductoPedido(Integer cantidad, Pedido pedido, Producto producto) {
        this.cantidad = cantidad;
        this.pedido = pedido;
        this.producto = producto;
    }
}
