package com.dcnt.take_away_now;

import com.dcnt.take_away_now.domain.Cliente;
import com.dcnt.take_away_now.domain.Plan;
import com.dcnt.take_away_now.service.ClienteService;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ClienteTests {
    private String username;
    private Cliente cliente;
    private Plan planPrime;

    @BeforeEach
    void setUp() {
        username = "Lionel Messi";
        cliente = new Cliente(username);
        planPrime = new Plan("Prime", new Dinero(2000), new PuntosDeConfianza(100), 25, 2, true, 0);
    }

    @Test
    void cumpleOkEnAnioBisiesto() {
        // given: "dado un cliente mayor de edad" al cual le establecemos su fecha de nacimiento
        cliente.establecerFechaDeNacimiento(
                LocalDate.of(1996, 2, 29),
                LocalDate.of(2024, 2, 29)
        );

        // then: "vemos si es su cumpleaños un año bisiesto"
        assertThat(
                cliente.esSuCumpleanios(
                        LocalDate.of(2024, 2, 29)
                )
        ).isTrue();
    }

    @Test
    void NoCumpleUnDiaPosteriorASuCumpleanios() {
        // given: "dado un cliente mayor de edad" al cual le establecemos su fecha de nacimiento
        Cliente cliente = new Cliente("Messi");
        cliente.establecerFechaDeNacimiento(
                LocalDate.of(1996, 2, 29),
                LocalDate.of(2024, 2, 29)
        );

        // then: "vemos si es su cumpleaños"
        assertThat(
                cliente.esSuCumpleanios(
                        LocalDate.of(2024, 3, 1)
                )
        ).isFalse();
    }

    @Test
    void PuedeCargarSaldoPositivo() {
        // when: "le cargamos 100 pesos a un cliente"
        cliente.cargarSaldo(new BigDecimal(100));

        // then: "vemos su dinero es 100 pesos"
        assertThat(cliente.getSaldo()).isEqualTo(new Dinero(100));
    }

    @Test
    void NoPuedeCargarSaldoNegativo() {
        // given: Dado un cliente

        // when: "le cargamos -100 pesos"

        // then: "vemos su dinero es el mismo anterior a la carga y obtenemos un error"
        assertThatThrownBy(
                () -> cliente.cargarSaldo(new BigDecimal(-100))
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se puede cargar saldo con un monto menor o igual a cero.");

        assertThat(cliente.getSaldo()).isEqualTo(new Dinero(0));
    }

    @Test
    void NoPuedeCargarSaldoCero() {
        // given: Dado un cliente

        // when: "le cargamos 0 pesos"

        // then: "vemos su dinero es el mismo anterior a la carga y obtenemos un error"
        assertThatThrownBy(
                () -> cliente.cargarSaldo(new BigDecimal(0))
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se puede cargar saldo con un monto menor o igual a cero.");

        assertThat(cliente.getSaldo()).isEqualTo(new Dinero(0));
    }

    @Test
    void UnClienteSinSaldoSuficienteNoPuedeAdquirirElPlanPrime() {
        // given: Dado un cliente

        // when: "quiere adquirir el plan prime sin tener dinero suficiente"

        // then: "vemos que su dinero es el mismo anterior a la carga y obtenemos un error"
        assertThatThrownBy(
                () -> cliente.obtenerPlanPrime(planPrime)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No posees saldo suficiente para adquirir el plan Prime.");

        assertThat(cliente.getSaldo()).isEqualTo(new Dinero(0));
    }

    @Test
    void UnClienteConSaldoSuficientePuedeAdquirirElPlanPrime() {
        // given: Dado un cliente con saldo suficiente para el plan prime
        cliente.setSaldo(new Dinero(2000));

        // when: "quiere adquirir el plan prime con el dinero suficiente"
        cliente.obtenerPlanPrime(planPrime);

        // then: "vemos que su dinero se redujo en funcion del valor del plan prime y el cliente ahora es prime"
        assertThat(cliente.getSaldo()).isEqualTo(new Dinero(0));
        assertThat(cliente.esPrime()).isTrue();
    }

    @Test
    void UnClienteNoPuedeAdquirirElPlanPrimeMasDeUnaVez() {
        // given: Dado un cliente con saldo suficiente para el plan prime
        cliente.setSaldo(new Dinero(4000));

        // when: "quiere adquirir el plan prime por una segunda vez"
        cliente.obtenerPlanPrime(planPrime);

        assertThatThrownBy(
                () -> cliente.obtenerPlanPrime(planPrime)
        )
                .isInstanceOf(RuntimeException.class).
                hasMessageContaining("Ya estás suscripto al plan Prime.");

        // then: "vemos que su dinero se redujo en funcion del valor del plan prime y el cliente ahora es prime"
        assertThat(cliente.getSaldo()).isEqualTo(new Dinero(2000));
        assertThat(cliente.esPrime()).isTrue();
    }

    @Test
    void UnClienteNoTieneSaldoSuficienteParaUnPedidoUnicamenteConDinero() {
        // given: Dado un cliente sin dinero.
        cliente.setSaldo(new Dinero(0));

        // when: "queremos ver si tiene saldo para un pedido a pagar unicamente con dinero."
        boolean tieneSaldoSuficiente = cliente.tieneSaldoSuficiente(new Dinero(1000), new PuntosDeConfianza(0), false);

        // then: "vemos que no tiene dinero suficiente."
        assertThat(tieneSaldoSuficiente).isFalse();
    }

    @Test
    void UnClienteNoTienePdcSuficienteParaUnPedidoUnicamenteConPdc() {
        // given: Dado un cliente sin pdc.
        cliente.setPuntosDeConfianza(new PuntosDeConfianza(0));

        // when: "queremos ver si tiene pdc para un pedido a pagar unicamente con pdc."
        boolean tienePdcSuficiente = cliente.tieneSaldoSuficiente(new Dinero(0), new PuntosDeConfianza(100), true);

        // then: "vemos que no tiene dinero suficiente."
        assertThat(tienePdcSuficiente).isFalse();
    }

    @Test
    void UnClienteTienePdcSuficientePeroNoTieneDineroSuficienteParaUnPedidoConUsoDeAmbos() {
        // given: Dado un cliente con pdc y dinero.
        cliente.setSaldo(new Dinero(0));
        cliente.setPuntosDeConfianza(new PuntosDeConfianza(100));

        // when: "queremos ver si tiene pdc y dinero para un pedido a pagar con ambos."
        boolean tienePdcYDineroSuficiente = cliente.tieneSaldoSuficiente(new Dinero(500), new PuntosDeConfianza(30), true);

        // then: "vemos que no tiene dinero suficiente."
        assertThat(tienePdcYDineroSuficiente).isFalse();
    }

    @Test
    void UnClienteTieneNoPdcSuficientePeroSiTieneDineroSuficienteParaUnPedidoConUsoDeAmbos() {
        // given: Dado un cliente con pdc y dinero.
        cliente.setSaldo(new Dinero(1000));
        cliente.setPuntosDeConfianza(new PuntosDeConfianza(0));

        // when: "queremos ver si tiene pdc y dinero para un pedido a pagar con ambos."
        boolean tienePdcYDineroSuficiente = cliente.tieneSaldoSuficiente(new Dinero(500), new PuntosDeConfianza(30), true);

        // then: "vemos que no tiene dinero suficiente."
        assertThat(tienePdcYDineroSuficiente).isFalse();
    }

    @Test
    void UnClienteTienePdcYDineroSuficienteParaUnPedidoConUsoDeAmbos() {
        // given: Dado un cliente con pdc y dinero.
        cliente.setSaldo(new Dinero(2500));
        cliente.setPuntosDeConfianza(new PuntosDeConfianza(100));

        // when: "queremos ver si tiene pdc y dinero para un pedido a pagar con ambos."
        boolean tienePdcYDineroSuficiente = cliente.tieneSaldoSuficiente(new Dinero(500), new PuntosDeConfianza(30), true);

        // then: "vemos que no tiene dinero suficiente."
        assertThat(tienePdcYDineroSuficiente).isTrue();
    }
}
