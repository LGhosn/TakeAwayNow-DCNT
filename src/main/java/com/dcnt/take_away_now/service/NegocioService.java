package com.dcnt.take_away_now.service;

import com.dcnt.take_away_now.domain.InventarioRegistro;
import com.dcnt.take_away_now.domain.Negocio;
import com.dcnt.take_away_now.domain.Producto;
import com.dcnt.take_away_now.dto.InventarioRegistroDto;
import com.dcnt.take_away_now.dto.PedidoDto;
import com.dcnt.take_away_now.dto.ProductoDto;
import com.dcnt.take_away_now.repository.InventarioRegistroRepository;
import com.dcnt.take_away_now.repository.NegocioRepository;
import com.dcnt.take_away_now.repository.PedidoRepository;
import com.dcnt.take_away_now.repository.ProductoRepository;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@AllArgsConstructor
@Service
public class NegocioService {
    private final NegocioRepository negocioRepository;
    private final InventarioRegistroRepository inventarioRegistroRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    public void crearNegocio(
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
            throw new RuntimeException("Ya existe un negocio con ese nombre.");
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
    }

    public Collection<Negocio> obtenerNegocios() {
        return negocioRepository.findAll();
    }

    public void crearProducto(
            Long negocioId,
            String nombreDelProducto,
            InventarioRegistroDto inventarioRegistroDto
    ) {
        // Corroboramos que exista el negocio.
        Optional<Negocio> optionalNegocio = negocioRepository.findById(negocioId);
        if (optionalNegocio.isEmpty()) {
            throw new RuntimeException("No existe el negocio para el cual busca crear el producto.");
        }
        Negocio negocioExistente = optionalNegocio.get();

        // Corroboramos que no exista un producto con el nombre pasado por parámetro para este negocio.
        Optional<Producto> optionalProducto = productoRepository.findByNombre(nombreDelProducto);
        if (optionalProducto.isPresent() && inventarioRegistroRepository.existsByNegocioAndProducto(optionalNegocio.get(), optionalProducto.get())) {
            throw new RuntimeException("Ya existe un producto con este nombre y para este negocio.");
        }

        // Creamos el nuevo producto y el registro.

        Long stock = inventarioRegistroDto.getStock();
        Dinero precio = inventarioRegistroDto.getPrecio();
        PuntosDeConfianza recompensa = inventarioRegistroDto.getRecompensaPuntosDeConfianza();
        PuntosDeConfianza precioPDC = inventarioRegistroDto.getPrecioPDC();

        InventarioRegistro nuevoInventarioRegistro = inventarioRegistroRepository.save(new InventarioRegistro(stock, precio, recompensa, precioPDC));
        Producto nuevoProducto = productoRepository.save(new Producto(nombreDelProducto));

        nuevoInventarioRegistro.setProducto(nuevoProducto);
        nuevoInventarioRegistro.setNegocio(negocioExistente);
        nuevoProducto.setInventarioRegistro(nuevoInventarioRegistro);

        productoRepository.save(nuevoProducto);
        inventarioRegistroRepository.save(nuevoInventarioRegistro);
    }

    public void eliminarProducto(Long negocioId, Long productoId) {
        Optional<Negocio> OptNegocio = negocioRepository.findById(negocioId);
        Optional<Producto> OptProducto = productoRepository.findById(productoId);

        // Corroboramos que dichos IDs pertenezcan a registros existentes en la DB.
        if (OptNegocio.isEmpty()) {
            throw new RuntimeException("El negocio para el cual busca eliminar dicho producto no existe.");
        }

        if (OptProducto.isEmpty()) {
            throw new RuntimeException("El producto que busca eliminar no existe.");
        }

        Optional<InventarioRegistro> OptInventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(OptNegocio.get(), OptProducto.get());
        // Corroboramos que exista la relación entre el negocio y el producto en cuestión.
        if (OptInventarioRegistro.isEmpty()) {
            throw new RuntimeException("El producto que busca eliminar no existe para el negocio en cuestión.");
        }

        // Borramos la relación de la tabla de correlación.
        OptProducto.get().setInventarioRegistro(null);
        if (OptNegocio.get().getInventarioRegistros() != null ) {
            OptNegocio.get().getInventarioRegistros().remove(OptInventarioRegistro.get());
        }
        inventarioRegistroRepository.deleteByNegocioAndProducto(OptNegocio.get(), OptProducto.get());
        // Borramos el producto de su respectiva tabla.
        productoRepository.deleteById(OptProducto.get().getId());
    }

    public Collection<ProductoDto> obtenerProductos(Long negocioId) {
        Optional<Negocio> optionalNegocio = negocioRepository.findById(negocioId);

        // Corroboramos la existencia del negocio.
        if (optionalNegocio.isEmpty()) {
            throw new NoSuchElementException("No existe el negocio al cual se solicitó obtener sus productos.");
        }

        return inventarioRegistroRepository.obtenerProductosDelNegocio(negocioId);
    }

    public void modificarInventarioRegistro(
            Long negocioId,
            Long productoId,
            Long stock,
            BigDecimal precio,
            Double recompensaPuntosDeConfianza,
            Double precioPdc
    ) {
        // Corroboramos la existencia del negocio.
        Optional<Negocio> optionalNegocio = negocioRepository.findById(negocioId);
        if (optionalNegocio.isEmpty()) {
            throw new RuntimeException("No existe el negocio al cual se solicitó modificar uno de sus productos.");
        }

        // Corroboramos la existencia del producto.
        Optional<Producto> optionalProducto = productoRepository.findById(productoId);
        if (optionalProducto.isEmpty()) {
            throw new RuntimeException("No existe el producto al cual se solicitó modificar.");
        }

        // Corroboramos que exista la relación entre el negocio y el producto en cuestión.
        Optional<InventarioRegistro> optInventarioRegistro = inventarioRegistroRepository.findByNegocioAndProducto(optionalNegocio.get(), optionalProducto.get());
        if (optInventarioRegistro.isEmpty()) {
            throw new RuntimeException("El producto que busca modificar no existe para el negocio en cuestión.");
        }

        // Finalmente modificamos el registro.
        InventarioRegistro inventarioRegistroExistente = optInventarioRegistro.get();
        inventarioRegistroExistente.setStock(stock);
        inventarioRegistroExistente.setPrecio(new Dinero(precio));
        inventarioRegistroExistente.setRecompensaPuntosDeConfianza(new PuntosDeConfianza(recompensaPuntosDeConfianza));
        inventarioRegistroExistente.setPrecioPDC(new PuntosDeConfianza(precioPdc));
        inventarioRegistroRepository.save(inventarioRegistroExistente);
    }

    public void modificarHorariosDelNegocio(Long negocioId, int horaApertura, int minutoApertura, int horaCierre, int minutoCierre) {
        LocalTime horarioApertura = LocalTime.of(horaApertura, minutoApertura);
        LocalTime horarioCierre = LocalTime.of(horaCierre, minutoCierre);

        // No existe el negocio para el cual se solicita cambiar el horario.
        Optional<Negocio> optionalNegocio = negocioRepository.findById(negocioId);
        if (optionalNegocio.isEmpty()) {
            throw new RuntimeException("No existe el negocio para el cual se solicita cambiar el horario.");
        }

        Negocio negocioExistente =  optionalNegocio.get();
        negocioExistente.setHorarioDeApertura(horarioApertura);
        negocioExistente.setHorarioDeCierre(horarioCierre);

        negocioRepository.save(negocioExistente);
    }

    public void modificarDiasDeAperturaDelNegocio(Long negocioId, DayOfWeek diaDeApertura, DayOfWeek diaDeCierre) {
        // No existe el negocio para el cual se solicita cambiar el horario.
        Optional<Negocio> optionalNegocio = negocioRepository.findById(negocioId);
        if (optionalNegocio.isEmpty()) {
            throw new RuntimeException("No existe el negocio para el cual se solicita cambiar los dias de apertura y cierre.");
        }

        Negocio negocioExistente =  optionalNegocio.get();
        negocioExistente.setDiaDeApertura(diaDeApertura);
        negocioExistente.setDiaDeCierre(diaDeCierre);

        negocioRepository.save(negocioExistente);
    }

    public Collection<PedidoDto> obtenerPedidos(Long idNegocio) {
        // Corroboramos la existencia del negocio
        negocioRepository.findById(idNegocio).orElseThrow( () -> new RuntimeException("No existe el negocio en la base de datos.") );

        return pedidoRepository.obtenerPedidosDelNegocio(idNegocio);
    }

    public Collection<Negocio> obtenerNegociosAbiertos() {
        Collection<Negocio> negocios = negocioRepository.findAll();
        List<Negocio> negociosAbiertos = new ArrayList<>();

        for (Negocio negocio : negocios) {
            if (negocio.estaAbierto(LocalDateTime.now())) {
                negociosAbiertos.add(negocio);
            }
        }

        return negociosAbiertos;
    }

    public Collection<Negocio> obtenerNegociosCerrados() {
        Collection<Negocio> negocios = negocioRepository.findAll();
        List<Negocio> negociosCerrados = new ArrayList<>();

        for (Negocio negocio : negocios) {
            if (negocio.estaCerrado(LocalDateTime.now())) {
                negociosCerrados.add(negocio);
            }
        }

        return negociosCerrados;
    }

    public Long corroborarExistencia(String nombre) {
        Optional<Negocio> n = negocioRepository.findByNombre(nombre);
        if (n.isEmpty()) {
            return (long)-1;
        }

        Negocio negocio = n.get();
        return negocio.getId();
    }

    public Negocio obtenerInfoNegocio(Long idNegocio) {
        return negocioRepository.findById(idNegocio).orElseThrow( () -> new RuntimeException("No existe el cliente en la base de datos.") );
    }
}
