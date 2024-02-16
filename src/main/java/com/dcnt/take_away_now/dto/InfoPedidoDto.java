package com.dcnt.take_away_now.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class InfoPedidoDto {
    private Long idCliente;
    private Long idNegocio;
    private Map<Long,Map<String, Object>> productos;
}
