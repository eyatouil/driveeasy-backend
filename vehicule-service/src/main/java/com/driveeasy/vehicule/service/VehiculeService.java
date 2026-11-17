package com.driveeasy.vehicule.service;

import com.driveeasy.vehicule.dto.StatutUpdateDTO;
import com.driveeasy.vehicule.dto.VehiculeCreateDTO;
import com.driveeasy.vehicule.dto.VehiculeDTO;
import com.driveeasy.vehicule.dto.VehiculeUpdateDTO;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.StatutVehicule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface VehiculeService {
    List<VehiculeDTO> getAllVehicules(String query, Carburant carburant, StatutVehicule statut, BigDecimal prixMax);
    VehiculeDTO getVehiculeById(Long id);
    VehiculeDTO createVehicule(VehiculeCreateDTO createDTO);
    VehiculeDTO updateVehicule(Long id, VehiculeUpdateDTO updateDTO);
    void deleteVehicule(Long id);
    VehiculeDTO updateStatut(Long id, StatutUpdateDTO statutDTO);
    List<VehiculeDTO> getVehiculesDisponibles(LocalDate dateDebut, LocalDate dateFin);
}
