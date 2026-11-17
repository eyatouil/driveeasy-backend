package com.driveeasy.client.repository;

import com.driveeasy.client.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    List<Client> findAll();
    Optional<Client> findById(Long id);
    Optional<Client> findByCin(String cin);
    Optional<Client> findByEmail(String email);
    Optional<Client> findByNumeroPermis(String numeroPermis);
    List<Client> search(String query);
    Client save(Client client);
    boolean deleteById(Long id);
    boolean existsById(Long id);
    boolean existsByCin(String cin);
    boolean existsByEmail(String email);
    boolean existsByNumeroPermis(String numeroPermis);
    long count();
}
