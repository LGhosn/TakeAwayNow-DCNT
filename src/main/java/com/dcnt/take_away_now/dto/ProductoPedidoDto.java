package com.dcnt.take_away_now.dto;

import com.dcnt.take_away_now.domain.Producto;
import com.dcnt.take_away_now.value_object.Dinero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductoPedidoDto {
    private Producto producto;
    private Integer cantidad;
    private Dinero precio;
}
