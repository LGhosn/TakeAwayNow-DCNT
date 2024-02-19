package com.dcnt.take_away_now.domain;


import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import com.dcnt.take_away_now.value_object.converter.PuntosDeConfianzaAttributeConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Entity
@Table(name = "BENEFICIOS")
public class Beneficio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_BENEFICIO")
    private Long id;

    @Column(name="NOMBRE")
    private String nombre;

    @Column(name="CANTIDAD_DE_PRODUCTOS_GRATIS")
    private int cantidadDeProductosGratis;

    @Column(name="PUNTOS_DE_CONFIANZA_ADICIONALES")
    @Convert(converter = PuntosDeConfianzaAttributeConverter.class)
    private PuntosDeConfianza puntosDeConfianzaAdicionales;

    public Beneficio(String nombre, int cantidadDeProductosGratis, PuntosDeConfianza puntosDeConfianza) {
        this.nombre = nombre;
        this.cantidadDeProductosGratis = cantidadDeProductosGratis;
        this.puntosDeConfianzaAdicionales = puntosDeConfianza;
    }
}
