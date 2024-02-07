package com.dcnt.take_away_now.repository;

import com.dcnt.take_away_now.domain.InventarioRegistro;
import com.dcnt.take_away_now.domain.Negocio;
import com.dcnt.take_away_now.domain.Producto;
import com.dcnt.take_away_now.dto.ProductoDto;
import jakarta.transaction.Transactional;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface InventarioRegistroRepository extends JpaRepository<InventarioRegistro, Long> {
    @Transactional
    void deleteByNegocioAndProducto(Negocio negocio, Producto producto);

    @Transactional
    Optional<InventarioRegistro> findByNegocioAndProducto(Negocio negocio, Producto producto);

    boolean existsByNegocioAndProducto(Negocio negocio, Producto producto);

    @Query( "SELECT new com.dcnt.take_away_now.dto.ProductoDto(p.id, p.nombre, ir.stock, ir.precio, ir.recompensaPuntosDeConfianza) " +
            "FROM InventarioRegistro ir "+
            "INNER JOIN Producto p on ir.producto.id = p.id " +
            "WHERE ir.negocio.id = :idNegocio")
    Collection<ProductoDto> obtenerProductosDelNegocio(@Param("idNegocio") Long idNegocio);
}
