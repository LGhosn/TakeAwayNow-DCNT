package com.dcnt.take_away_now.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PRODUCTOS")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_PRODUCTO")
    private Long id;
    @Column(name="NOMBRE")
    public String nombre;
    @JsonBackReference
    @OneToOne(targetEntity = InventarioRegistro.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_INVENTARIO_REGISTRO")
    private InventarioRegistro inventarioRegistro;

    public Producto(String nombre) {
        this.nombre = nombre;
    }
}
