package com.dcnt.take_away_now.controller;

import com.dcnt.take_away_now.domain.Negocio;
import com.dcnt.take_away_now.dto.InventarioRegistroDto;
import com.dcnt.take_away_now.repository.NegocioRepository;
import com.dcnt.take_away_now.repository.ProductoRepository;
import com.dcnt.take_away_now.service.NegocioService;
import com.dcnt.take_away_now.value_object.Dinero;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.swing.text.html.Option;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers =  NegocioController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class NegocioControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NegocioService negocioService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NegocioRepository negocioRepository;
    @MockBean
    private ProductoRepository productoRepository;
    private DayOfWeek DiaDeApertura;
    private DayOfWeek DiaDeCierre;
    private int HoraApertura;
    private int MinutoApertura;
    private int HoraCierre;
    private int MinutoCierre;
    private String nombrePaseoColon;
    @BeforeEach
    void setUp() {
        DiaDeApertura = DayOfWeek.MONDAY;
        DiaDeCierre = DayOfWeek.FRIDAY;
        HoraApertura = 9;
        MinutoApertura = 0;
        HoraCierre = 18;
        MinutoCierre = 0;
        nombrePaseoColon = "Buffet Paseo Colon";
    }

    @Test
    void sePuedeCrearNegocio() throws  Exception {
        ResultActions response = mockMvc.perform(post("/api/negocios/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("nombre", "Paseo Colon")
                .param("diaDeApertura", String.valueOf(DiaDeApertura))
                .param("diaDeCierre", String.valueOf(DiaDeCierre))
                .param("horaApertura", String.valueOf(HoraApertura))
                .param("minutoApertura", String.valueOf(MinutoApertura))
                .param("horaCierre", String.valueOf(HoraCierre))
                .param("minutoCierre", String.valueOf(MinutoCierre))
        );
        response.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void noSePuedeCrearNegocioSiFaltanDatos() throws  Exception {
        ResultActions response = mockMvc.perform(post("/api/negocios/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("diaDeApertura", String.valueOf(DiaDeApertura))
                .param("diaDeCierre", String.valueOf(DiaDeCierre))
                .param("horaApertura", String.valueOf(HoraApertura))
                .param("minutoApertura", String.valueOf(MinutoApertura))
                .param("horaCierre", String.valueOf(HoraCierre))
                .param("minutoCierre", String.valueOf(MinutoCierre))
        );
        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    void sePuedeCrearProductoNuevo() throws Exception {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);

        //TODO REVISAR TODOS LOS IDS HARDOCEADOS
        ResultActions response = mockMvc.perform(post("/api/negocios/" + 1 + "/productos/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("nombreDelProducto", "Pancho")
                        .param("stock", "5")
                        .param("precio", "100")
                        .param("recompensaPuntosDeConfianza", "20")
                        .param("precioPdc", "20")
        );

        response.andExpect(MockMvcResultMatchers.status().isOk());
    }

    /* TODO VERIFICAR PORQUE EL SEGUNDO POST TIENE STATUS 200 CUANDO TIENE QUE SER 500
    @Test
    void noSePuedeCrearProductoQueYaExisteEnEseNegocio() throws Exception {
        //given
        negocioService.crearNegocio(nombrePaseoColon, DiaDeApertura, DiaDeCierre,HoraApertura, MinutoApertura, HoraCierre, MinutoCierre);
        InventarioRegistroDto inventarioRegistroDto = new InventarioRegistroDto(10L, new Dinero(100), new PuntosDeConfianza(20));
        Optional<Negocio> negocio = negocioRepository.findByNombre(nombrePaseoColon);
        negocioService.crearProducto(negocio.get().getId(), "Pancho",inventarioRegistroDto);

        //when
        ResultActions response = mockMvc.perform(post("/api/negocios/" + 1 + "/productos/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("nombreDelProducto", "Pancho")
                .param("stock", "5")
                .param("precio", "100")
                .param("recompensaPuntosDeConfianza", "20")
        );

        response.andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }*/

    @Test
    void obtenerProductos() throws Exception {
        //given
        mockMvc.perform(post("/api/negocios/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("nombre", "Paseo Colon")
                .param("diaDeApertura", String.valueOf(DiaDeApertura))
                .param("diaDeCierre", String.valueOf(DiaDeCierre))
                .param("horaApertura", String.valueOf(HoraApertura))
                .param("minutoApertura", String.valueOf(MinutoApertura))
                .param("horaCierre", String.valueOf(HoraCierre))
                .param("minutoCierre", String.valueOf(MinutoCierre))
        );

        mockMvc.perform(post("/api/negocios/" + 1 + "/productos/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("nombreDelProducto", "Pancho")
                .param("stock", "5")
                .param("precio", "100")
                .param("recompensaPuntosDeConfianza", "20")
        );
        mockMvc.perform(post("/api/negocios/" + 1 + "/productos/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("nombreDelProducto", "Coca Cola")
                .param("stock", "5")
                .param("precio", "100")
                .param("recompensaPuntosDeConfianza", "20")
        );
        mockMvc.perform(post("/api/negocios/" + 1 + "/productos/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("nombreDelProducto", "Alfajor")
                .param("stock", "5")
                .param("precio", "100")
                .param("recompensaPuntosDeConfianza", "20")
        );
        ResultActions response = mockMvc.perform(get("/api/negocios/"+ 1 + "/productos")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void obtenerNegocios() throws  Exception {
        ResultActions response = mockMvc.perform(get("/api/negocios/")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void modificarProducto() {
    }

    @Test
    void modificarHorariosDelNegocio() {
    }

    @Test
    void modificarDiasDeAperturaDelNegocio() {
    }

    @Test
    void eliminarProducto() {
    }
}