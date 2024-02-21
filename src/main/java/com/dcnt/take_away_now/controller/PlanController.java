package com.dcnt.take_away_now.controller;

import com.dcnt.take_away_now.domain.Plan;
import com.dcnt.take_away_now.service.PlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

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
    public ResponseEntity<String> crearPlan(@RequestParam String nombre, @RequestParam int precio, @RequestParam int puntosDeConfianza, @RequestParam int descuento, @RequestParam int multiplicadorPuntoDeConfianza, @RequestParam boolean cancelacionSinCosto, @RequestParam int porcentajeDePuntosDeConfianzaPorDevolucion) {
        try {
            planService.crearPlan(nombre, precio, puntosDeConfianza, descuento, multiplicadorPuntoDeConfianza, cancelacionSinCosto, porcentajeDePuntosDeConfianzaPorDevolucion);
        } catch (RuntimeException e ) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Plan creado con éxito.");

    }

    /*******************
     *   Métodos Get *
     *******************/

    @GetMapping("/")
    public Collection<Plan> obtenerPlanes() {
        return planService.obtenerPlanes();
    }

    /*******************
     *   Métodos Delete *
     ********************/

    @DeleteMapping("/{planId}")
    public ResponseEntity<String> eliminarPlan(@PathVariable Long planId) {
        try {
            planService.eliminarPlan(planId);
        } catch (RuntimeException e ) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Plan eliminado con éxito.");
    }

}