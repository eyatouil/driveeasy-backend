package com.driveeasy.vehicule.repository;

import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.StatutVehicule;
import com.driveeasy.vehicule.model.Vehicule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VehiculeRepository {
    List<Vehicule> findAll();
    Optional<Vehicule> findById(Long id);
    Optional<Vehicule> findByImmatriculation(String immatriculation);
    List<Vehicule> findWithFilters(String marqueOrModele, Carburant carburant, StatutVehicule statut, BigDecimal prixMax);
    List<Vehicule> findDisponibles();
    Vehicule save(Vehicule vehicule);
    boolean deleteById(Long id);
    boolean existsById(Long id);
    boolean existsByImmatriculation(String immatriculation);
    long count();
}
