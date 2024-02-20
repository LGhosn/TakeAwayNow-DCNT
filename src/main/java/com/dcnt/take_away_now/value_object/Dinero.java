package com.dcnt.take_away_now.value_object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dinero implements Comparable<Dinero> {
    public BigDecimal monto;

    public Dinero(int i) {
        this.monto = BigDecimal.valueOf(i);
    }

    public BigDecimal toBigDecimal() {
        return getMonto();
    }

    /**
     *
     * Retorna un nuevo dinero resultante de sumar los montos.
     *
     */
    public Dinero plus(Dinero otro) {
        BigDecimal resultado = this.getMonto().add(otro.getMonto());
        return new Dinero(resultado);
    }

    /**
     *
     * Retorna un nuevo dinero resultante de restar los montos. Si el monto resultante
     * es negativo se lanza un error.
     *
     */
    public Dinero minus(Dinero otro) {
        BigDecimal resultado = this.getMonto().subtract(otro.getMonto());
        return new Dinero(resultado);
    }

    /**
     *
     * Compara dinero segun el monto.
     *
     */
    public int compareTo(Dinero otro) {
        if (this.getMonto() == null || otro.getMonto() == null) {
            throw new IllegalArgumentException("Los montos no pueden ser nulos");
        }
        return this.getMonto().compareTo(otro.getMonto());
    }

    /**
     *
     * Multiplica el monto actual y la cantidad indicada. Si la cantidad es negativa se lanza
     * un error.
     *
     */
    public Dinero multiply(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalStateException("No se puede multiplicar el dinero por cero o un numero negativo.");
        }
        BigDecimal resultado = this.getMonto().multiply(BigDecimal.valueOf(cantidad));
        return new Dinero(resultado);
    }

    public Dinero multiply(Dinero otro) {
        BigDecimal resultado = this.getMonto().multiply(otro.getMonto());
        return new Dinero(resultado);
    }

    /**
     *
     * Divide el monto actual y la cantidad indicada. Si la cantidad es negativa se lanza
     * un error.
     *
     */
    public Dinero divide(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalStateException("No se puede dividir el dinero por cero o un numero negativo.");
        }
        BigDecimal resultado = this.getMonto().divide(BigDecimal.valueOf(cantidad));
        return new Dinero(resultado);
    }
}
