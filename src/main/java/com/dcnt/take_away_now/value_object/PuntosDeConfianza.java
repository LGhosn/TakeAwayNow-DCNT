package com.dcnt.take_away_now.value_object;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class PuntosDeConfianza {
    private final Double cantidad;

    public PuntosDeConfianza(int cantidadInicial) {
        this.cantidad = (double) cantidadInicial;
    }

    public PuntosDeConfianza(Double cantidadInicial) {
        this.cantidad = cantidadInicial;
    }

    /**
     *
     * Suma puntos de confianza con un entero. Retorna puntos de confianza
     * con la cantidad restultante de sumar la actual con la indicada. Si la cantidad
     * indicada es negativa se lanza un error.
     *
     */
    public PuntosDeConfianza plus(Double cantidad) {
        if (cantidad <= 0) {
            throw new IllegalStateException("La cantidad a agregar no puede ser negativa o cero");
        }
        return new PuntosDeConfianza(this.cantidad + cantidad);
    }

    public PuntosDeConfianza plus(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalStateException("La cantidad a agregar no puede ser negativa o cero");
        }
        return new PuntosDeConfianza(this.cantidad + cantidad);
    }

    /**
     *
     * Resta puntos de confianza con un entero. Retorna puntos de confianza
     * con la cantidad restultante de restar la actual con la indicada por parametro.
     * Si resta da menor a cero se lanza un error.
     * Si la cantidad indicada por parametro es negativa se lanza un error.
     */
    public PuntosDeConfianza minus(Double cantidadPorRestar) {
        if (cantidadPorRestar < 0) {
            throw new IllegalStateException("La cantidad de puntos de confianza a restar no puede ser negativa.");
        }
        return new PuntosDeConfianza(this.cantidad - cantidadPorRestar);
    }

    public PuntosDeConfianza minus(int cantidadPorRestar) {
        if (cantidadPorRestar < 0) {
            throw new IllegalStateException("La cantidad de puntos de confianza a restar no puede ser negativa.");
        }
        return new PuntosDeConfianza(this.cantidad - cantidadPorRestar);
    }

    /**
     *
     * Suma la cantidad actual con la cantidad de los puntos de confianza recibidos y retorna
     * nuevos puntos de confianza con el valor resultante
     *
     */
    public PuntosDeConfianza plus(PuntosDeConfianza otro) {
        return new PuntosDeConfianza(this.cantidad + otro.cantidad);
    }

    /**
     *
     * Resta la cantidad actual con la cantidad de puntos de confianza recibidos y retorna
     * nuevos puntos de confianza con el valor resultante.
     * Si resta da menor a cero se lanza un error.
     * Si la cantidad indicada por parametro es negativa se lanza un error.
     */
    public PuntosDeConfianza minus(PuntosDeConfianza puntosPorRestar) {
        return new PuntosDeConfianza(this.getCantidad() - puntosPorRestar.getCantidad());
    }

    /**
     *
     * Multiplica la cantidad actual por la cantidad indicada por parámetro.
     * Retorna una instancia de PuntosDeConfianza con la cantidad resultante.
     * Si esta cantidad indicada por parámetro es negativa se lanza un error.
     */
    public PuntosDeConfianza multiply(Double cantidad) {
        if (cantidad < 0) {
            throw new IllegalStateException("No se pueden multiplicar puntos de confianza por números menores a cero.");
        }
        return new PuntosDeConfianza(this.cantidad * cantidad);
    }

    public PuntosDeConfianza multiply(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalStateException("No se pueden multiplicar puntos de confianza por números menores a cero.");
        }
        return new PuntosDeConfianza(this.cantidad * cantidad);
    }

    /*
    PuntosDeConfianza eliminarPuntosPorCompra(Compra compra, int multiplicador = 1) {
        this - this.calcularPuntosPorCompra(compra) * multiplicador
    }

    PuntosDeConfianza agregarPuntosPorCompra(Compra compra, int multiplicador = 1) {
        this + this.calcularPuntosPorCompra(compra) * multiplicador
    }


    PuntosDeConfianza calcularPuntosPorCompra(Compra compra) {
        new PuntosDeConfianza(compra.cantidadDeProductosPorDinero())
    }
    */
}
