package com.driveeasy.vehicule.repository;

import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.StatutVehicule;
import com.driveeasy.vehicule.model.Vehicule;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryVehiculeRepository implements VehiculeRepository {

    private final Map<Long, Vehicule> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(100);

    @Override
    public List<Vehicule> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Vehicule> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Vehicule> findByImmatriculation(String immatriculation) {
        if (immatriculation == null) return Optional.empty();
        return store.values().stream()
                .filter(v -> immatriculation.trim().equalsIgnoreCase(v.getImmatriculation() != null ? v.getImmatriculation().trim() : ""))
                .findFirst();
    }

    @Override
    public List<Vehicule> findWithFilters(String marqueOrModele, Carburant carburant, StatutVehicule statut, BigDecimal prixMax) {
        return store.values().stream()
                .filter(v -> {
                    if (marqueOrModele != null && !marqueOrModele.isBlank()) {
                        String query = marqueOrModele.toLowerCase().trim();
                        boolean matchMarque = v.getMarque() != null && v.getMarque().toLowerCase().contains(query);
                        boolean matchModele = v.getModele() != null && v.getModele().toLowerCase().contains(query);
                        if (!matchMarque && !matchModele) return false;
                    }
                    if (carburant != null && v.getCarburant() != carburant) {
                        return false;
                    }
                    if (statut != null && v.getStatut() != statut) {
                        return false;
                    }
                    if (prixMax != null && v.getPrixJournalier() != null && v.getPrixJournalier().compareTo(prixMax) > 0) {
                        return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Vehicule::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Vehicule> findDisponibles() {
        return store.values().stream()
                .filter(v -> v.getStatut() == StatutVehicule.DISPONIBLE)
                .sorted(Comparator.comparing(Vehicule::getId))
                .collect(Collectors.toList());
    }

    @Override
    public Vehicule save(Vehicule vehicule) {
        if (vehicule.getId() == null) {
            vehicule.setId(idGenerator.incrementAndGet());
        }
        store.put(vehicule.getId(), vehicule);
        return vehicule;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) return false;
        return store.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && store.containsKey(id);
    }

    @Override
    public boolean existsByImmatriculation(String immatriculation) {
        return findByImmatriculation(immatriculation).isPresent();
    }

    @Override
    public long count() {
        return store.size();
    }
}
