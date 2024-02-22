package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.Pedido;
import com.dcnt.take_away_now.dto.PedidoDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    boolean existsPedidoById(Long idPedido);

    @Query( "SELECT new com.dcnt.take_away_now.dto.PedidoDto(p.id, p.cliente.usuario, p.negocio.nombre, p.estado, p.precioTotal, p.fechaYHoraDeEntrega, p.codigoDeRetiro, ir.precioPDC)" +
            "FROM Pedido p "+
            "INNER JOIN Cliente c on p.cliente.id = c.id " +
            "INNER JOIN Negocio n on p.negocio.id = n.id " +
            "INNER JOIN InventarioRegistro ir on ir.negocio.id = n.id " +
            "WHERE c.id = :idCliente")
    Collection<PedidoDto> obtenerPedidosDelCliente(@Param("idCliente") Long idCliente);

    @Query( "SELECT new com.dcnt.take_away_now.dto.PedidoDto(p.id, p.cliente.usuario, p.negocio.nombre, p.estado, p.precioTotal, p.fechaYHoraDeEntrega, p.codigoDeRetiro, ir.precioPDC)" +
            "FROM Pedido p "+
            "INNER JOIN Cliente c on p.cliente.id = c.id " +
            "INNER JOIN Negocio n on p.negocio.id = n.id " +
            "INNER JOIN InventarioRegistro ir on ir.negocio.id = n.id " +
            "WHERE n.id = :idNegocio")
    Collection<PedidoDto> obtenerPedidosDelNegocio(@Param("idNegocio") Long idNegocio);
}
