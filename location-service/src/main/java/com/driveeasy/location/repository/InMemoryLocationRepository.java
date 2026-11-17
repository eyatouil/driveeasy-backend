package com.driveeasy.location.repository;

import com.driveeasy.location.model.Location;
import com.driveeasy.location.model.StatutLocation;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryLocationRepository implements LocationRepository {

    private final Map<Long, Location> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(100);

    @Override
    public List<Location> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Location::getId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Location> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Location> findByClientId(Long clientId) {
        if (clientId == null) return Collections.emptyList();
        return store.values().stream()
                .filter(l -> clientId.equals(l.getClientId()))
                .sorted(Comparator.comparing(Location::getId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Location> findByVehiculeId(Long vehiculeId) {
        if (vehiculeId == null) return Collections.emptyList();
        return store.values().stream()
                .filter(l -> vehiculeId.equals(l.getVehiculeId()))
                .sorted(Comparator.comparing(Location::getId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Location> findWithFilters(StatutLocation statut, Long clientId, Long vehiculeId, LocalDate dateDebut, LocalDate dateFin) {
        return store.values().stream()
                .filter(l -> {
                    if (statut != null && l.getStatut() != statut) return false;
                    if (clientId != null && !clientId.equals(l.getClientId())) return false;
                    if (vehiculeId != null && !vehiculeId.equals(l.getVehiculeId())) return false;
                    if (dateDebut != null && l.getDateDebut() != null && l.getDateDebut().isBefore(dateDebut)) return false;
                    if (dateFin != null && l.getDateFinPrevue() != null && l.getDateFinPrevue().isAfter(dateFin)) return false;
                    return true;
                })
                .sorted(Comparator.comparing(Location::getId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public long countCompletedByClientId(Long clientId) {
        if (clientId == null) return 0;
        return store.values().stream()
                .filter(l -> clientId.equals(l.getClientId()) && l.getStatut() == StatutLocation.TERMINEE)
                .count();
    }

    @Override
    public long countByStatut(StatutLocation statut) {
        if (statut == null) return 0;
        return store.values().stream()
                .filter(l -> l.getStatut() == statut)
                .count();
    }

    @Override
    public Location save(Location location) {
        if (location.getId() == null) {
            location.setId(idGenerator.incrementAndGet());
        }
        store.put(location.getId(), location);
        return location;
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
    public long count() {
        return store.size();
    }
}
