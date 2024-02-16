package com.dcnt.take_away_now.domain;

import com.dcnt.take_away_now.dto.InventarioRegistroDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import com.dcnt.take_away_now.value_object.converter.DineroAttributeConverter;
import com.dcnt.take_away_now.value_object.converter.PuntosDeConfianzaAttributeConverter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "INVENTARIO_REGISTROS")
public class InventarioRegistro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INVENTARIO_REGISTRO")
    private Long id;

    @ManyToOne(targetEntity = Negocio.class)
    @JoinColumn(name = "ID_NEGOCIO")
    private Negocio negocio;

    @OneToOne(targetEntity = Producto.class,cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ID_PRODUCTO")
    private Producto producto;

    @Column(name="STOCK")
    private Long stock;

    @Column(name="PRECIO_UNITARIO")
    @Convert(converter = DineroAttributeConverter.class)
    private Dinero precio;

    @Column(name="PRECIO_UNITARIO_PDC")
    @Convert(converter = PuntosDeConfianzaAttributeConverter.class)
    private PuntosDeConfianza precioPDC;

    @Column(name="RECOMPENSA_PDC")
    @Convert(converter = PuntosDeConfianzaAttributeConverter.class)
    private PuntosDeConfianza recompensaPuntosDeConfianza;

    public InventarioRegistro(InventarioRegistroDto dto) {
        this.stock = dto.getStock();
        this.precio = dto.getPrecio();
        this.recompensaPuntosDeConfianza = dto.getRecompensaPuntosDeConfianza();
    }
}
