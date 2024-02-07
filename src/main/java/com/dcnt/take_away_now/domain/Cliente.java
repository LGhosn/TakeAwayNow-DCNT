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

    public Cliente(String nombreDeUsuario) {
        this.usuario = nombreDeUsuario;
    }

}
