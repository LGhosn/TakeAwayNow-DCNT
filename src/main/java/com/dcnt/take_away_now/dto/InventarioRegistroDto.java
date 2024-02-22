package com.dcnt.take_away_now.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
public class InventarioRegistroDto {
    private Long Stock;
    private Dinero precio;
    private PuntosDeConfianza recompensaPuntosDeConfianza;
    private PuntosDeConfianza precioPDC;

    public InventarioRegistroDto(
        Long stockIngresado,
        Dinero precioIngresado,
        PuntosDeConfianza recompensaPuntosDeConfianzaIngresada,
        PuntosDeConfianza precioPDCIngresado
    ) {
        if (stockIngresado <= 0) {
            throw new RuntimeException("No se permite ingresar un stock negativo o igual a cero.");
        }

        if (precioIngresado.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("No se permite ingresar un precio negativo o igual a cero.");
        }

        if (recompensaPuntosDeConfianzaIngresada.getCantidad() <= 0) {
            throw new RuntimeException("No se permite ingresar una recompensa negativa o igual a cero.");
        }

        if (precioPDCIngresado.getCantidad() <= 0) {
            throw new RuntimeException("No se permite ingresar un precio de PDC negativo o igual a cero.");
        }

        this.Stock = stockIngresado;
        this.precio = precioIngresado;
        this.recompensaPuntosDeConfianza = recompensaPuntosDeConfianzaIngresada;
        this.precioPDC = precioPDCIngresado;
    }
}

