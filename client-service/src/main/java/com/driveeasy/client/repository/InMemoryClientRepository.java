package com.driveeasy.client.repository;

import com.driveeasy.client.model.Client;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryClientRepository implements ClientRepository {

    private final Map<Long, Client> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(100);

    @Override
    public List<Client> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Client::getId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Client> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Client> findByCin(String cin) {
        if (cin == null) return Optional.empty();
        return store.values().stream()
                .filter(c -> cin.trim().equalsIgnoreCase(c.getCin() != null ? c.getCin().trim() : ""))
                .findFirst();
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return store.values().stream()
                .filter(c -> email.trim().equalsIgnoreCase(c.getEmail() != null ? c.getEmail().trim() : ""))
                .findFirst();
    }

    @Override
    public Optional<Client> findByNumeroPermis(String numeroPermis) {
        if (numeroPermis == null) return Optional.empty();
        return store.values().stream()
                .filter(c -> numeroPermis.trim().equalsIgnoreCase(c.getNumeroPermis() != null ? c.getNumeroPermis().trim() : ""))
                .findFirst();
    }

    @Override
    public List<Client> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String q = query.toLowerCase().trim();
        return store.values().stream()
                .filter(c -> {
                    boolean matchNom = c.getNom() != null && c.getNom().toLowerCase().contains(q);
                    boolean matchPrenom = c.getPrenom() != null && c.getPrenom().toLowerCase().contains(q);
                    boolean matchCin = c.getCin() != null && c.getCin().toLowerCase().contains(q);
                    boolean matchEmail = c.getEmail() != null && c.getEmail().toLowerCase().contains(q);
                    return matchNom || matchPrenom || matchCin || matchEmail;
                })
                .sorted(Comparator.comparing(Client::getId))
                .collect(Collectors.toList());
    }

    @Override
    public Client save(Client client) {
        if (client.getId() == null) {
            client.setId(idGenerator.incrementAndGet());
        }
        store.put(client.getId(), client);
        return client;
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
    public boolean existsByCin(String cin) {
        return findByCin(cin).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByNumeroPermis(String numeroPermis) {
        return findByNumeroPermis(numeroPermis).isPresent();
    }

    @Override
    public long count() {
        return store.size();
    }
}
