package com.dcnt.take_away_now.dto;

import com.dcnt.take_away_now.domain.Pedido;
import com.dcnt.take_away_now.enums.EstadoDelPedido;
import com.dcnt.take_away_now.value_object.Dinero;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PedidoDto {
    private Long idPedido;
    private String usuarioCliente;
    private String negocio;
    private Pedido.EstadoDelPedido estado;
    private Dinero precioTotal;
    private LocalDateTime fechaYHoraDeEntrega;
    private String codigoDeRetiro;

    public PedidoDto(
            Long idPedido,
            String usuarioCliente,
            String negocio,
            Pedido.EstadoDelPedido estado,
            Dinero precioTotal,
            LocalDateTime fechaYHoraDeEntrega,
            String codigoDeRetiro
    ) {
        this.idPedido = idPedido;
        this.usuarioCliente = usuarioCliente;
        this.negocio = negocio;
        this.estado = estado;
        this.precioTotal = precioTotal;
        this.fechaYHoraDeEntrega = fechaYHoraDeEntrega;
        this.codigoDeRetiro = codigoDeRetiro;
    }
}
