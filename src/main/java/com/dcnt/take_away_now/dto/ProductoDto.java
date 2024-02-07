package com.dcnt.take_away_now.dto;

import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductoDto {
    private Long id;
    private String nombre;
    private Long Stock;
    private Dinero precio;
    private PuntosDeConfianza recompensaPuntosDeConfianza;
}
