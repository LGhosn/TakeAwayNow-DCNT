package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.ProductoPedido;
import com.dcnt.take_away_now.dto.PedidoDto;
import com.dcnt.take_away_now.dto.ProductoPedidoDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ProductoPedidoRepository extends JpaRepository<ProductoPedido, Long> {
    @Query("SELECT new com.dcnt.take_away_now.dto.ProductoPedidoDto(pp.producto, pp.cantidad, ir.precio, ir.precioPDC) " +
            "FROM ProductoPedido pp " +
            "INNER JOIN InventarioRegistro ir on ir.producto.id = pp.producto.id " +
            "WHERE pp.pedido.id = :idPedido"
    )
    Collection<ProductoPedidoDto> obtenerProductosDelPedido(@Param("idPedido") Long idPedido);
}
