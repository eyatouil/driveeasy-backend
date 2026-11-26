package com.driveeasy.location.client;

import com.driveeasy.location.client.dto.StatutUpdatePayload;
import com.driveeasy.location.client.dto.VehiculeResponseDTO;

import java.util.List;

public interface VehiculeServiceClient {
    VehiculeResponseDTO getVehiculeById(Long id);
    List<VehiculeResponseDTO> getVehiculesDisponibles();
    VehiculeResponseDTO updateStatut(Long id, StatutUpdatePayload payload);

    /** Parc complet (tous statuts) : sert au calcul du taux d'occupation. */
    List<VehiculeResponseDTO> getTousVehicules();
}
