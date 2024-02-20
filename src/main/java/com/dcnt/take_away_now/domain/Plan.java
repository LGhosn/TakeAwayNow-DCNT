package com.dcnt.take_away_now.domain;

import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;
import com.dcnt.take_away_now.value_object.converter.PuntosDeConfianzaAttributeConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "PLANES")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_PLAN")
    private Long id;

    @Column(name="NOMBRE")
    private String nombre;

    @Column(name="PRECIO")
    @Convert(converter = DineroAttributeConverter.class)
    private Dinero precio;

    @Column(name="PUNTOS_DE_CONFIANZA_POR_SUBSCRIPCION")
    @Convert(converter = PuntosDeConfianzaAttributeConverter.class)
    private PuntosDeConfianza puntosDeConfianzaPorSubscripcion;

    @Column(name="PORCENTAJE_DE_DESCUENTO")
    private int descuento;

    @Column(name="MULTIPLICADOR_DE_PUNTOS_DE_CONFIANZA")
    private int multiplicadorDePuntosDeConfianza;

    @Column(name="CANCELACION_SIN_COSTO")
    private boolean cancelacionSinCosto;

    @Column(name="PORCENTAJE_EXTRA_DE_PUNTOS_DE_CONFIANZA_POR_DEVOLUCION")
    private int porcentajeExtraDePuntosDeConfianzaPorDevolucion;

    public Plan (String nombre, Dinero precio, PuntosDeConfianza puntosDeConfianza, int descuento, int multiplicadorDePuntosDeConfianza, boolean cancelacionSinCosto, int porcentajeExtraDePuntosDeConfianzaPorDevolucion) {
        this.nombre = nombre;
        this.precio = precio;
        this.puntosDeConfianzaPorSubscripcion = puntosDeConfianza;
        this.descuento = descuento;
        this.multiplicadorDePuntosDeConfianza = multiplicadorDePuntosDeConfianza;
        this.cancelacionSinCosto = cancelacionSinCosto;
        this.porcentajeExtraDePuntosDeConfianzaPorDevolucion = porcentajeExtraDePuntosDeConfianzaPorDevolucion;
    }
}