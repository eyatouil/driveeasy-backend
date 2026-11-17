package com.driveeasy.location.repository;

import com.driveeasy.location.model.Location;
import com.driveeasy.location.model.StatutLocation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    List<Location> findAll();
    Optional<Location> findById(Long id);
    List<Location> findByClientId(Long clientId);
    List<Location> findByVehiculeId(Long vehiculeId);
    List<Location> findWithFilters(StatutLocation statut, Long clientId, Long vehiculeId, LocalDate dateDebut, LocalDate dateFin);
    long countCompletedByClientId(Long clientId);
    long countByStatut(StatutLocation statut);
    Location save(Location location);
    boolean deleteById(Long id);
    boolean existsById(Long id);
    long count();
}
