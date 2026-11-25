package com.driveeasy.location.controller;

import com.driveeasy.location.dto.*;
import com.driveeasy.location.model.StatutLocation;
import com.driveeasy.location.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Liste des locations, filtrable (statut, client, véhicule, période).
     * Pagination : paramètres optionnels page (défaut 0) et size (défaut : tout).
     * Le corps reste un tableau JSON ; les métadonnées sont dans les en-têtes
     * X-Total-Count, X-Total-Pages, X-Page et X-Page-Size.
     */
    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations(
            @RequestParam(required = false) StatutLocation statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(locationService.getAllLocations(statut, clientId, vehiculeId, dateDebut, dateFin));
        }
        PageResult<LocationDTO> result = locationService.getLocationsPage(
                statut, clientId, vehiculeId, dateDebut, dateFin,
                page != null ? page : 0, size != null ? size : 10);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(result.getTotalPages()))
                .header("X-Page", String.valueOf(result.getPage()))
                .header("X-Page-Size", String.valueOf(result.getSize()))
                .body(result.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationEnrichieDTO> getLocationEnrichie(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationEnrichie(id));
    }

    @PostMapping
    public ResponseEntity<LocationDTO> createReservation(@Valid @RequestBody LocationCreateDTO createDTO) {
        LocationDTO created = locationService.createReservation(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/simulation")
    public ResponseEntity<SimulationResponseDTO> simulerPrix(@Valid @RequestBody SimulationRequestDTO simulationDTO) {
        return ResponseEntity.ok(locationService.simulerPrix(simulationDTO));
    }

    @PatchMapping("/{id}/depart")
    public ResponseEntity<LocationDTO> demarrerLocation(
            @PathVariable Long id,
            @RequestBody(required = false) DepartRequestDTO departDTO) {
        return ResponseEntity.ok(locationService.demarrerLocation(id, departDTO));
    }

    @PatchMapping("/{id}/retour")
    public ResponseEntity<LocationDTO> retournerVehicule(
            @PathVariable Long id,
            @Valid @RequestBody RetourRequestDTO retourDTO) {
        return ResponseEntity.ok(locationService.retournerVehicule(id, retourDTO));
    }

    @PatchMapping("/{id}/annulation")
    public ResponseEntity<LocationDTO> annulerLocation(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.annulerLocation(id));
    }

    /** Utilisé par vehicule-service pour calculer les véhicules disponibles sur une période. */
    @GetMapping("/vehicules-occupes")
    public ResponseEntity<List<Long>> getVehiculesOccupes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(locationService.getVehiculesOccupes(dateDebut, dateFin));
    }

    @GetMapping("/statistiques")
    public ResponseEntity<StatistiquesDTO> getStatistiques() {
        return ResponseEntity.ok(locationService.getStatistiques());
    }
}
