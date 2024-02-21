package com.dcnt.take_away_now.domain;

import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;
import com.dcnt.take_away_now.value_object.converter.PuntosDeConfianzaAttributeConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CLIENTES")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_CLIENTE")
    private Long id;

    @Column(name="USUARIO")
    public String usuario;

    @Column(name="SALDO")
    @Convert(converter = DineroAttributeConverter.class)
    public Dinero saldo = new Dinero(0);

    @Column(name="PDC")
    @Convert(converter = PuntosDeConfianzaAttributeConverter.class)
    private PuntosDeConfianza puntosDeConfianza= new PuntosDeConfianza((double) 0);

    @JsonBackReference
    @OneToMany(targetEntity = Pedido.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "cliente")
    private List<Pedido> pedidos;

    @ManyToOne(targetEntity = Plan.class)
    @JoinColumn(name="ID_PLAN")
    private Plan plan = null;

    @Column(name="FECHA_NACIMIENTO")
    private LocalDate fechaDeNacimiento = null;

    @Column(name="FECHA_ULT_USO_BENEF_CUMPLE")
    private LocalDate fechaUltUsoBenefCumple = null;

    public Cliente(String nombreDeUsuario) {
        this.usuario = nombreDeUsuario;
    }

    public boolean esPrime() { return plan != null; }

    public boolean esSuCumpleanios() {
        LocalDate hoy = LocalDate.now();
        return fechaDeNacimiento != null && hoy.getDayOfMonth() == fechaDeNacimiento.getDayOfMonth() && hoy.getMonth() == fechaDeNacimiento.getMonth();
    }

    public boolean todaviaNoUsaBeneficioCumple() {
        LocalDate hoy = LocalDate.now();

        /* En caso de utilizarlo por primera vez */
        if (fechaUltUsoBenefCumple == null) {
            return true;
        }

        /* En caso de utilizarlo más de una vez */
        return fechaUltUsoBenefCumple != null && fechaUltUsoBenefCumple.getYear() != hoy.getYear();
    }

    public void cargarSaldo(BigDecimal saldoACargar) {
        if (saldoACargar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("No se puede cargar saldo con un monto menor o igual a cero.");
        }
        this.setSaldo(this.getSaldo().plus(new Dinero(saldoACargar)));
    }

    public void establecerFechaDeNacimiento(LocalDate fechaDeNacimiento, LocalDate hoy) {
        if (this.getFechaDeNacimiento() != null) {
            throw new RuntimeException("No se puede cambiar la fecha de nacimiento una vez establecida.");
        }

        if (Period.between(fechaDeNacimiento, hoy).getYears() < 18) {
            throw new RuntimeException("Debes ser mayor de edad para acceder al beneficio por cumpleaños.");
        }

        this.setFechaDeNacimiento(fechaDeNacimiento);
    }

    public void obtenerPlanPrime(Plan plan) {
        BigDecimal saldoCliente = this.getSaldo().getMonto();
        BigDecimal precioPlanPrime = plan.getPrecio().getMonto();

        if (this.esPrime()) {
            throw new RuntimeException("Ya estás suscripto al plan Prime.");
        }

        if (saldoCliente.compareTo(precioPlanPrime) < 0) {
            throw new RuntimeException("No posees saldo suficiente para adquirir el plan Prime.");
        }

        // Guardamos la relación entre cliente y plan.
        this.setPlan(plan);
    }
    public boolean tieneSaldoSuficiente(Dinero precioTotalDelPedido, PuntosDeConfianza pdcTotalDelPedido) {
        // Tomamos los montos a comparar.
        BigDecimal montoActualTotalPedido = precioTotalDelPedido.getMonto();
        BigDecimal montoTotalSaldoCliente = this.getSaldo().getMonto();

        Double cantidadPDCActualTotalPedido = pdcTotalDelPedido.getCantidad();
        Double cantidadPDCTotalCliente = this.getPuntosDeConfianza().getCantidad();

        if (montoActualTotalPedido.compareTo(montoTotalSaldoCliente) > 0) {
            return false;
        }

        if (cantidadPDCActualTotalPedido.compareTo(cantidadPDCTotalCliente) > 0) {
            return false;
        }
        return  true;
    }
}
