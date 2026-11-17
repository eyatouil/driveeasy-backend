package com.driveeasy.vehicule.controller;

import com.driveeasy.vehicule.dto.StatutUpdateDTO;
import com.driveeasy.vehicule.dto.VehiculeCreateDTO;
import com.driveeasy.vehicule.dto.VehiculeDTO;
import com.driveeasy.vehicule.dto.VehiculeUpdateDTO;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.StatutVehicule;
import com.driveeasy.vehicule.service.VehiculeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    public VehiculeController(VehiculeService vehiculeService) {
        this.vehiculeService = vehiculeService;
    }

    @GetMapping
    public ResponseEntity<List<VehiculeDTO>> getAllVehicules(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Carburant carburant,
            @RequestParam(required = false) StatutVehicule statut,
            @RequestParam(required = false) BigDecimal prixMax) {
        return ResponseEntity.ok(vehiculeService.getAllVehicules(query, carburant, statut, prixMax));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<VehiculeDTO>> getVehiculesDisponibles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(vehiculeService.getVehiculesDisponibles(dateDebut, dateFin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeDTO> getVehiculeById(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.getVehiculeById(id));
    }

    @PostMapping
    public ResponseEntity<VehiculeDTO> createVehicule(@Valid @RequestBody VehiculeCreateDTO createDTO) {
        VehiculeDTO created = vehiculeService.createVehicule(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculeDTO> updateVehicule(
            @PathVariable Long id,
            @Valid @RequestBody VehiculeUpdateDTO updateDTO) {
        return ResponseEntity.ok(vehiculeService.updateVehicule(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        vehiculeService.deleteVehicule(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<VehiculeDTO> updateStatut(
            @PathVariable Long id,
            @Valid @RequestBody StatutUpdateDTO statutDTO) {
        return ResponseEntity.ok(vehiculeService.updateStatut(id, statutDTO));
    }
}
