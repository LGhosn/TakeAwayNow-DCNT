package com.dcnt.take_away_now;

import com.dcnt.take_away_now.domain.Negocio;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class NegocioTests {

    private LocalTime HoraDeApertura;
    private LocalTime HoraDeCierre;
    private DayOfWeek DiaDeApertura;
    private DayOfWeek DiaDeCierre;
    LocalDateTime UnSabadoPorLaTarde;
    LocalDateTime UnJuevesPorElMedioDia;
    LocalDateTime UnMartesPorLaMadrugada;

    @BeforeEach
    void setUp() {
        HoraDeApertura = LocalTime.of(9, 0);
        HoraDeCierre = LocalTime.of(18, 0);
        DiaDeApertura = DayOfWeek.MONDAY;
        DiaDeCierre = DayOfWeek.FRIDAY;
        UnSabadoPorLaTarde = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY)), LocalTime.of(18, 0));
        UnJuevesPorElMedioDia = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY)), LocalTime.of(13, 45));
        UnMartesPorLaMadrugada = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY)), LocalTime.of(3, 20));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void unNegocioSeEncuentraCerradoSiAbreDeLunesAViernesYPreguntoUnSabado() {
        // given: "horarios de apertura y cierre, y días de la semana para la apertura y el cierre"
        Negocio negocioResultante = new Negocio(
                "Buffet Paseo Colón",
                HoraDeApertura,
                HoraDeCierre,
                DiaDeApertura,
                DiaDeCierre
        );

        // when: "quiero ver si el negocio está cerrado un sábado por la tarde"
        boolean estaCerrado = negocioResultante.estaCerrado(UnSabadoPorLaTarde);

        // then: "se lanza error"
        assertThat(estaCerrado).isTrue();
    }

    @Test
    void unNegocioSeEncuentraAbiertoSiAbreDeLunesAViernesYPreguntoUnJueves() {
        // given: "un negocio correctamente creado"
        Negocio negocioResultante = new Negocio(
                "Buffet Paseo Colón",
                HoraDeApertura,
                HoraDeCierre,
                DiaDeApertura,
                DiaDeCierre
        );

        // when: "quiero ver si el negocio está abierto un jueves por el medio día"
        boolean estaAbierto = negocioResultante.estaAbierto(UnJuevesPorElMedioDia);

        // then: "se lanza error"
        assertThat(estaAbierto).isTrue();
    }

    @Test
    void unNegocioSeEncuentraCerradoSiAbreDeLunesAViernesDe9A18YPreguntoUnMartesPorLaMadrugada() {
        // given: "un negocio correctamente creado"
        Negocio negocioResultante = new Negocio(
                "Buffet Paseo Colón",
                HoraDeApertura,
                HoraDeCierre,
                DiaDeApertura,
                DiaDeCierre
        );

        // when: "quiero ver si el negocio está abierto un jueves por el medio día"
        boolean estaCerrado = negocioResultante.estaCerrado(UnMartesPorLaMadrugada);

        // then: "se lanza error"
        assertThat(estaCerrado).isTrue();
    }
}
