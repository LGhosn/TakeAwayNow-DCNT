package com.dcnt.take_away_now.domain;

import com.dcnt.take_away_now.repository.BeneficioRepository;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @Column(name="ID_PLAN")
    private long IdPlan = -1;

    @Column(name="FECHA_SUBSCRIPCION")
    private LocalDateTime fechaSubscripcion;

    @Column(name="FECHA_DE_CUMPLEANIOS")
    private LocalDateTime fechaDeCumpleanios;

    @Column(name="FECHA_CANJE_POR_CUMPLEANIOS")
    private LocalDateTime fechaCanjePorCumpleanios;

    @JsonBackReference
    @OneToMany(targetEntity = Pedido.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "cliente")
    private List<Pedido> pedidos;


    public Cliente(String nombreDeUsuario, LocalDateTime fechaDeCumpleanios) {
        this.usuario = nombreDeUsuario;
        this.fechaDeCumpleanios = fechaDeCumpleanios;
    }

    public boolean beneficioDeCumpleanios() {
        boolean esCumpleanios =  this.fechaDeCumpleanios.equals(LocalDateTime.now());
        boolean noFueCanjeado = this.fechaCanjePorCumpleanios == null || this.fechaCanjePorCumpleanios.getYear() != LocalDateTime.now().getYear();
        return esCumpleanios && noFueCanjeado;
    }

    public void seCanjeoBeneficioDeCumpleanios() {
        this.fechaCanjePorCumpleanios = LocalDateTime.now();
    }
}
