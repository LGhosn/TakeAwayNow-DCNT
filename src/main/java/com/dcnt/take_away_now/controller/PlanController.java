package com.dcnt.take_away_now.controller;

import com.dcnt.take_away_now.service.PlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/planes")
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    /******************
     *   Métodos Post *
     ******************/

    @PostMapping("/")
    public ResponseEntity<String> crearPlan(@RequestParam String nombre, @RequestParam int precio, @RequestParam int duracion, @RequestParam int puntosDeConfianza, @RequestParam int descuento, @RequestParam int multiplicadorPuntoDeConfianza, @RequestParam boolean cancelacionSinCosto, @RequestParam int porcentajeDePuntosDeConfianzaPorDevolucion) {
        return planService.crearPlan(nombre, precio, duracion, puntosDeConfianza, descuento, multiplicadorPuntoDeConfianza, cancelacionSinCosto, porcentajeDePuntosDeConfianzaPorDevolucion);
    }

    /*******************
     *   Métodos Get *
     *******************/

    @GetMapping("/")
    public ResponseEntity<String> obtenerPlanes() {
        return planService.obtenerPlanes();
    }

    /*******************
     *   Métodos Delete *
     ********************/

    @DeleteMapping("/{planId}")
    public ResponseEntity<String> eliminarPlan(@PathVariable Long planId) {
        return planService.eliminarPlan(planId);
    }

}
