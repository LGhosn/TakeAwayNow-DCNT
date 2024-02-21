package com.dcnt.take_away_now.domain;

import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;
import com.dcnt.take_away_now.value_object.converter.PuntosDeConfianzaAttributeConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
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

    @Column(name="ID_PLAN")
    private Long idPlanPrime = null;

    @Column(name="FECHA_NACIMIENTO")
    private LocalDate fechaDeNacimiento = null;

    @Column(name="FECHA_ULT_USO_BENEF_CUMPLE")
    private LocalDate fechaUltUsoBenefCumple = null;

    public Cliente(String nombreDeUsuario) {
        this.usuario = nombreDeUsuario;
    }

    public boolean esPrime() { return idPlanPrime != null; }

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

}
