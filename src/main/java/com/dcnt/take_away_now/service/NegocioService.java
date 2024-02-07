package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.InventarioRegistro;
import com.dcnt.take_away_now.domain.Negocio;
import com.dcnt.take_away_now.domain.Pedido;
import com.dcnt.take_away_now.domain.Producto;
import com.dcnt.take_away_now.dto.InventarioRegistroDto;
import com.dcnt.take_away_now.dto.PedidoDto;
import com.dcnt.take_away_now.dto.ProductoDto;
import com.dcnt.take_away_now.enums.EstadoDelPedido;
import com.dcnt.take_away_now.repository.InventarioRegistroRepository;
import com.dcnt.take_away_now.repository.NegocioRepository;
import com.dcnt.take_away_now.repository.PedidoRepository;
import com.dcnt.take_away_now.repository.ProductoRepository;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import lombok.AllArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
@SuppressWarnings("OptionalGetWithoutIsPresent")
@AllArgsConstructor
@Service
public class NegocioService {
    private final NegocioRepository negocioRepository;
    private final InventarioRegistroRepository inventarioRegistroRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    public ResponseEntity<HttpStatus> crearNegocio(
            String nombre,
            DayOfWeek diaDeApertura,
            DayOfWeek diaDeCierre,
            int horaApertura,
            int minutoApertura,
            int horaCierre,
            int minutoCierre
    ) {
        Optional<Negocio> optionalNegocio = negocioRepository.findByNombre(nombre);
        if (optionalNegocio.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        this.negocioRepository.save(
                new Negocio(
                        nombre,
                        LocalTime.of(horaApertura,minutoApertura),
                        LocalTime.of(horaCierre,minutoCierre),
                        diaDeApertura,
                        diaDeCierre
                )
        );
        return ResponseEntity.ok().build();
    }

    public Collection<Negocio> obtenerNegocios() {
        return negocioRepository.findAll();
    }

    public ResponseEntity<HttpStatus> crearProducto(
            Long negocioId,
            String nombreDelProducto,
            InventarioRegistroDto inventarioRegistroDto
    ) {
        // Corroboramos que exista el negocio.
        Optional<Negocio> optionalNegocio = negocioRepository.findById(negocioId);
        if (optionalNegocio.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Corroboramos que no exista un producto con el nombre pasado por parámetro para este negocio.
        Optional<Producto> optionalProducto = productoRepository.findByNombre(nombreDelProducto);
        if (optionalProducto.isPresent() && inventarioRegistroRepository.existsByNegocioAndProducto(optionalNegocio.get(), optionalProducto.get())) {
            return ResponseEntity.internalServerError().build();
        }

        // Creamos el nuevo producto y el registro.
        InventarioRegistro nuevoInventarioRegistro = inventarioRegistroRepository.save(new InventarioRegistro(inventarioRegistroDto));
        Producto nuevoProducto = productoRepository.save(new Producto(nombreDelProducto));
        Negocio negocioExistente = optionalNegocio.get();

        nuevoInventarioRegistro.setProducto(nuevoProducto);
        nuevoInventarioRegistro.setNegocio(negocioExistente);
        nuevoProducto.setInventarioRegistro(nuevoInventarioRegistro);

        productoRepository.save(nuevoProducto);
        inventarioRegistroRepository.save(nuevoInventarioRegistro);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<HttpStatus> eliminarProducto(Long negocioId, Long productoId) {
        Optional<Negocio> OptNegocio = negocioRepository.findById(negocioId);
        Optional<Producto> OptProducto = productoRepository.findById(productoId);

        // Corroboramos que dichos IDs pertenezcan a registros existentes en la DB.
        if (OptNegocio.isEmpty() || OptProducto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<InventarioRegistro> OptInventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(OptNegocio.get(), OptProducto.get());
        // Corroboramos que exista la relación entre el negocio y el producto en cuestión.
        if (OptInventarioRegistro.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Borramos la relación de la tabla de correlación.
        OptProducto.get().setInventarioRegistro(null);
        //TODO REVISAR CONDICION
        if (OptNegocio.get().getInventarioRegistros() != null ) {
            OptNegocio.get().getInventarioRegistros().remove(OptInventarioRegistro.get());
        }
        inventarioRegistroRepository.deleteByNegocioAndProducto(OptNegocio.get(), OptProducto.get());
        // Borramos el producto de su respectiva tabla.
        productoRepository.deleteById(OptProducto.get().getId());
        return ResponseEntity.ok().build();
    }

    public Collection<ProductoDto> obtenerProductos(Long negocioId) {
        Optional<Negocio> optionalNegocio = negocioRepository.findById(negocioId);

        // Corroboramos la existencia del negocio.
        if (optionalNegocio.isEmpty()) {
            throw new NoSuchElementException("No existe el negocio al cual se solicitó obtener sus productos.");
        }

        return inventarioRegistroRepository.obtenerProductosDelNegocio(negocioId);
    }

    public ResponseEntity<HttpStatus> modificarInventarioRegistro(
            Long negocioId,
            Long productoId,
            Long stock,
            BigDecimal precio,
            Double recompensaPuntosDeConfianza
    ) {
        // Corroboramos la existencia del negocio.
        Optional<Negocio> optionalNegocio = negocioRepository.findById(negocioId);
        if (optionalNegocio.isEmpty()) {
            throw new NoSuchElementException("No existe el negocio al cual se solicitó modificar uno de sus productos.");
        }

        // Corroboramos la existencia del producto.
        Optional<Producto> optionalProducto = productoRepository.findById(productoId);
        if (optionalProducto.isEmpty()) {
            throw new NoSuchElementException("No existe el producto al cual se solicitó modificar.");
        }

        // Corroboramos que exista la relación entre el negocio y el producto en cuestión.
        Optional<InventarioRegistro> optInventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(optionalNegocio.get(), optionalProducto.get());
        if (optInventarioRegistro.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Finalmente modificamos el registro.
        InventarioRegistro inventarioRegistroExistente = optInventarioRegistro.get();
        inventarioRegistroExistente.setStock(stock);
        inventarioRegistroExistente.setPrecio(new Dinero(precio));
        inventarioRegistroExistente.setRecompensaPuntosDeConfianza(new PuntosDeConfianza(recompensaPuntosDeConfianza));
        inventarioRegistroRepository.save(inventarioRegistroExistente);

        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<HttpStatus> modificarHorariosDelNegocio(Long negocioId, int horaApertura, int minutoApertura, int horaCierre, int minutoCierre) {
        LocalTime horarioApertura = LocalTime.of(horaApertura, minutoApertura);
        LocalTime horarioCierre = LocalTime.of(horaCierre, minutoCierre);

        // El horario de apertura debe ser anterior al de cierre.
        if (horarioApertura.isAfter(horarioCierre)) {
            return ResponseEntity.badRequest().build();
        }

        // No existe el negocio para el cual se solicita cambiar el horario.
        Optional<Negocio> optionalNegocio = negocioRepository.findById(negocioId);
        if (optionalNegocio.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Negocio negocioExistente =  optionalNegocio.get();
        negocioExistente.setHorarioDeApertura(horarioApertura);
        negocioExistente.setHorarioDeCierre(horarioCierre);

        negocioRepository.save(negocioExistente);

        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<HttpStatus> modificarDiasDeAperturaDelNegocio(Long negocioId, DayOfWeek diaDeApertura, DayOfWeek diaDeCierre) {
        // No existe el negocio para el cual se solicita cambiar el horario.
        Optional<Negocio> optionalNegocio = negocioRepository.findById(negocioId);
        if (optionalNegocio.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Negocio negocioExistente =  optionalNegocio.get();
        negocioExistente.setDiaDeApertura(diaDeApertura);
        negocioExistente.setDiaDeCierre(diaDeCierre);

        negocioRepository.save(negocioExistente);

        return ResponseEntity.accepted().build();
    }

    public Collection<PedidoDto> obtenerPedidos(Long idNegocio) {
        // Corroboramos la existencia del negocio
        negocioRepository.findById(idNegocio).orElseThrow( () -> new RuntimeException("No existe el negocio en la base de datos.") );

        return pedidoRepository.obtenerPedidosDelNegocio(idNegocio);
    }
}
