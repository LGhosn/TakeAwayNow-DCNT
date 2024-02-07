package com.dcnt.take_away_now.dto;

import com.dcnt.take_away_now.domain.Producto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductoPedidoDto {
    private Producto producto;
    private Integer cantidad;
}
