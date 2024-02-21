package com.dcnt.take_away_now.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;

import java.time.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@Entity
@Table(name = "NEGOCIOS")
public class Negocio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_NEGOCIO")
    private Long id;

    @Column(name="NOMBRE")
    public String nombre;

    @Column(name="SALDO")
    @Convert(converter = DineroAttributeConverter.class)
    public Dinero saldo;

    @Column(name="DIA_APERTURA")
    public DayOfWeek diaDeApertura;

    @Column(name="DIA_CIERRE")
    public DayOfWeek diaDeCierre;

    @Column(name="HORARIO_APERTURA")
    public LocalTime horarioDeApertura;

    @Column(name="HORARIO_CIERRE")
    public LocalTime horarioDeCierre;

    @JsonBackReference
    @OneToMany(targetEntity = InventarioRegistro.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "negocio")
    private List<InventarioRegistro> inventarioRegistros;

    @JsonBackReference
    @OneToMany(targetEntity = Pedido.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "negocio")
    private List<Pedido> pedidos;

    /**
     *
     * Crea un negocio. Si el horario de apertura es mayor al de cierre se lanza un error.
     *
     */
    public Negocio(
            String nombreDelNegocio,
            LocalTime horarioDeApertura,
            LocalTime horarioDeCierre,
            DayOfWeek diaDeApertura,
            DayOfWeek diaDeCierre
    ) {
        this.nombre = nombreDelNegocio;
        this.horarioDeApertura = horarioDeApertura;
        this.horarioDeCierre = horarioDeCierre;
        this.diaDeApertura = diaDeApertura;
        this.diaDeCierre = diaDeCierre;
        this.saldo = new Dinero(0);
    }

    /**
     *
     * Indica si el negocio está cerrado según el horario.
     *
     */
    public boolean estaCerrado(LocalDateTime FechaYHora) {
        return !estaAbierto(FechaYHora);
    }

    /**
     *
     * Indica si el negocio esta abierto según el horario.
     *
     */
    public boolean estaAbierto(LocalDateTime FechaYHora) {
        // Obtener la zona horaria de Buenos Aires, y posteriormente la utilizamos para comparar los horarios.
        ZoneId zonaHorariaArgentina = ZoneId.of("America/Argentina/Buenos_Aires");
        ZonedDateTime fechaYHoraArgentina = FechaYHora.atZone(zonaHorariaArgentina);

        // Obtenemos el horario y el día en Argentina.
        LocalTime horarioIngresado = fechaYHoraArgentina.toLocalTime();
        DayOfWeek diaIngresado = fechaYHoraArgentina.getDayOfWeek();

        // Resto del código sigue igual
        return (
                ((diaIngresado.compareTo(diaDeApertura) >= 0) && (diaIngresado.compareTo(diaDeCierre) <= 0))
                        && (horarioIngresado.isAfter(horarioDeApertura) || horarioIngresado.equals(horarioDeApertura))
                        && (horarioIngresado.isBefore(horarioDeCierre) || horarioIngresado.equals(horarioDeCierre))
        );
    }
}