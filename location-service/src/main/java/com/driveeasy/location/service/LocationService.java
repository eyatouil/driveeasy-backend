package com.driveeasy.location.service;

import com.driveeasy.location.dto.*;
import com.driveeasy.location.model.StatutLocation;

import java.time.LocalDate;
import java.util.List;

public interface LocationService {
    List<LocationDTO> getAllLocations(StatutLocation statut, Long clientId, Long vehiculeId, LocalDate dateDebut, LocalDate dateFin);
    LocationEnrichieDTO getLocationEnrichie(Long id);
    LocationDTO createReservation(LocationCreateDTO createDTO);
    SimulationResponseDTO simulerPrix(SimulationRequestDTO simulationDTO);
    LocationDTO demarrerLocation(Long id, DepartRequestDTO departDTO);
    LocationDTO retournerVehicule(Long id, RetourRequestDTO retourDTO);
    LocationDTO annulerLocation(Long id);
    StatistiquesDTO getStatistiques();
}
