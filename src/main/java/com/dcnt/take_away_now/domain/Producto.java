package com.dcnt.take_away_now.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PRODUCTOS")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_PRODUCTO")
    private Long id;
    @Column(name="NOMBRE")
    public String nombre;
    @JsonBackReference
    @OneToOne(targetEntity = InventarioRegistro.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_INVENTARIO_REGISTRO")
    private InventarioRegistro inventarioRegistro;

    public Producto(String nombre) {
        this.nombre = nombre;
    }
    /*public Producto(String nombre, Integer montoPrecio, Integer cantidadPuntosDeConfianza) {
        if (montoPrecio <= 0) {
            throw new IllegalStateException("Un producto no puede ser creado con un precio menor o igual a cero.");
        }
        if (cantidadPuntosDeConfianza < 0) {
            throw new IllegalStateException("Un producto no puede ser creado con una cantidad negativa de Puntos de Confianza como recompensa.");
        }
        this.nombre = nombre;
    }*/
    /**
     * Devuelve el precio del Producto en función de la cantidad pasada por parámetro.

    Dinero precioPorCantidad(Integer cantidad) {
        return this.precio.multiply(cantidad);
    }*/

    /**
     * Devuelve los PuntosDeConfianza del Producto en función de la cantidad pasada por parámetro.

    PuntosDeConfianza puntosDeConfianzaPorCantidad(Integer cantidad) {
        return this.recompensaPuntosDeConfianza.multiply(cantidad);
    }*/

    /*

    Producto retirar(int cantidad) {
        if (this.cantidad < cantidad) throw new IllegalStateException("La cantidad que se desea retirar es mayor al stock actual del producto")
        this.cantidad -= cantidad
        new Producto(this.nombre, cantidad, this.precio, this.puntosDeConfianza);
    }
    * */

    /**
     * Devuelve un booleano indicando si es canjeable por PuntosDeConfianza

    boolean esCanjeablePorPuntosDeConfianza() {
        return !Objects.equals(this.getRecompensaPuntosDeConfianza(), new PuntosDeConfianza(0));
    }*/
}
