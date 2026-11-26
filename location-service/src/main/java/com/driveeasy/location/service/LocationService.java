package com.driveeasy.location.service;

import com.driveeasy.location.dto.*;
import com.driveeasy.location.dto.PageResult;
import com.driveeasy.location.model.StatutLocation;

import java.time.LocalDate;
import java.util.List;

public interface LocationService {
    List<LocationDTO> getAllLocations(StatutLocation statut, Long clientId, Long vehiculeId, LocalDate dateDebut, LocalDate dateFin);
    /** Liste paginée (page commence à 0). */
    PageResult<LocationDTO> getLocationsPage(StatutLocation statut, Long clientId, Long vehiculeId, LocalDate dateDebut, LocalDate dateFin, int page, int size);
    LocationEnrichieDTO getLocationEnrichie(Long id);
    LocationDTO createReservation(LocationCreateDTO createDTO);
    SimulationResponseDTO simulerPrix(SimulationRequestDTO simulationDTO);
    LocationDTO demarrerLocation(Long id, DepartRequestDTO departDTO);
    LocationDTO retournerVehicule(Long id, RetourRequestDTO retourDTO);
    LocationDTO annulerLocation(Long id);
    StatistiquesDTO getStatistiques();

    /** IDs des véhicules ayant une location RESERVEE/EN_COURS chevauchant la période. */
    java.util.List<Long> getVehiculesOccupes(java.time.LocalDate dateDebut, java.time.LocalDate dateFin);
}
