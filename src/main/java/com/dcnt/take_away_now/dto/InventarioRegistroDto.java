package com.dcnt.take_away_now.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class InventarioRegistroDto {
    private Long Stock;
    private Dinero precio;
    private PuntosDeConfianza recompensaPuntosDeConfianza;
    private PuntosDeConfianza precioPDC;
}
