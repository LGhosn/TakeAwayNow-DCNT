package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.Negocio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class NegocioRepositoryTest {
    @Autowired
    private NegocioRepository negocioRepository;
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
    void SeEncuentraUnNegocioQueExiste() {
        //given: un negocio llamado Buffet Paseo Colón
        Negocio negocioResultante = new Negocio(
                "Buffet Paseo Colón",
                HoraDeApertura,
                HoraDeCierre,
                DiaDeApertura,
                DiaDeCierre
        );
        negocioRepository.save(negocioResultante);
        //when: se busca por el negocio Buffet Paseo Colón
        boolean found = negocioRepository.findByNombre("Buffet Paseo Colón").isPresent();
        //then se lo encuentra
        assertThat(found).isTrue();
    }

    @Test
    void NoSeEncuentraUnNegocioQueNoExiste() {
        //given: un negocio llamado Buffet Paseo Colón
        Negocio negocioResultante = new Negocio(
                "Buffet Paseo Colón",
                HoraDeApertura,
                HoraDeCierre,
                DiaDeApertura,
                DiaDeCierre
        );
        negocioRepository.save(negocioResultante);
        //when: se busca por el negocio "Buffet"
        boolean found = negocioRepository.findByNombre("Buffet").isPresent();
        //then: No se encuentra el negocio
        assertThat(found).isFalse();
    }
}