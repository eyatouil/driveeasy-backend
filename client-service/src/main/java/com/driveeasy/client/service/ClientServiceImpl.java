package com.driveeasy.client.service;

import com.driveeasy.client.dto.*;
import com.driveeasy.client.exception.DuplicateResourceException;
import com.driveeasy.client.exception.InvalidOperationException;
import com.driveeasy.client.exception.ResourceNotFoundException;
import com.driveeasy.client.model.CategorieClient;
import com.driveeasy.client.model.Client;
import com.driveeasy.client.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public List<ClientDTO> searchClients(String query) {
        return clientRepository.search(query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClientDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client avec l'ID " + id + " non trouvé"));
        return toDTO(client);
    }

    @Override
    public ClientDTO createClient(ClientCreateDTO dto) {
        if (clientRepository.existsByCin(dto.getCin())) {
            throw new DuplicateResourceException("Un client avec le CIN '" + dto.getCin() + "' existe déjà");
        }
        if (clientRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Un client avec l'email '" + dto.getEmail() + "' existe déjà");
        }
        if (clientRepository.existsByNumeroPermis(dto.getNumeroPermis())) {
            throw new DuplicateResourceException("Un client avec le numéro de permis '" + dto.getNumeroPermis() + "' existe déjà");
        }

        Client client = new Client();
        client.setCin(dto.getCin().trim().toUpperCase());
        client.setNom(dto.getNom().trim());
        client.setPrenom(dto.getPrenom().trim());
        client.setDateNaissance(dto.getDateNaissance());
        client.setEmail(dto.getEmail().trim().toLowerCase());
        client.setTelephone(dto.getTelephone().trim());
        client.setAdresse(dto.getAdresse().trim());
        client.setNumeroPermis(dto.getNumeroPermis().trim().toUpperCase());
        client.setDateExpirationPermis(dto.getDateExpirationPermis());
        client.setCategorieClient(dto.getCategorieClient() != null ? dto.getCategorieClient() : CategorieClient.STANDARD);
        client.setDateInscription(LocalDate.now());

        Client saved = clientRepository.save(client);
        return toDTO(saved);
    }

    @Override
    public ClientDTO updateClient(Long id, ClientUpdateDTO dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client avec l'ID " + id + " non trouvé"));

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String newEmail = dto.getEmail().trim().toLowerCase();
            clientRepository.findByEmail(newEmail).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("L'adresse email '" + newEmail + "' est déjà utilisée");
                }
            });
            client.setEmail(newEmail);
        }

        if (dto.getNumeroPermis() != null && !dto.getNumeroPermis().isBlank()) {
            String newPermis = dto.getNumeroPermis().trim().toUpperCase();
            clientRepository.findByNumeroPermis(newPermis).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("Le numéro de permis '" + newPermis + "' est déjà utilisé");
                }
            });
            client.setNumeroPermis(newPermis);
        }

        if (dto.getNom() != null && !dto.getNom().isBlank()) client.setNom(dto.getNom().trim());
        if (dto.getPrenom() != null && !dto.getPrenom().isBlank()) client.setPrenom(dto.getPrenom().trim());
        if (dto.getDateNaissance() != null) client.setDateNaissance(dto.getDateNaissance());
        if (dto.getTelephone() != null && !dto.getTelephone().isBlank()) client.setTelephone(dto.getTelephone().trim());
        if (dto.getAdresse() != null && !dto.getAdresse().isBlank()) client.setAdresse(dto.getAdresse().trim());
        if (dto.getDateExpirationPermis() != null) client.setDateExpirationPermis(dto.getDateExpirationPermis());
        if (dto.getCategorieClient() != null) client.setCategorieClient(dto.getCategorieClient());

        Client updated = clientRepository.save(client);
        return toDTO(updated);
    }

    @Override
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client avec l'ID " + id + " non trouvé");
        }
        clientRepository.deleteById(id);
    }

    @Override
    public EligibiliteDTO checkEligibilite(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client avec l'ID " + id + " non trouvé"));

        LocalDate today = LocalDate.now();
        int age = Period.between(client.getDateNaissance(), today).getYears();
        boolean ageValide = age >= 21;
        boolean permisValide = client.getDateExpirationPermis() != null && client.getDateExpirationPermis().isAfter(today);

        List<String> motifs = new ArrayList<>();
        if (!ageValide) {
            motifs.add("Âge insuffisant : " + age + " ans (minimum requis : 21 ans)");
        }
        if (!permisValide) {
            motifs.add("Permis de conduire expiré ou invalide (date d'expiration : " + client.getDateExpirationPermis() + ")");
        }

        EligibiliteDTO eligibilite = new EligibiliteDTO();
        eligibilite.setClientId(client.getId());
        eligibilite.setNomComplet(client.getPrenom() + " " + client.getNom());
        eligibilite.setAge(age);
        eligibilite.setAgeValide(ageValide);
        eligibilite.setPermisValide(permisValide);
        eligibilite.setDateExpirationPermis(client.getDateExpirationPermis());
        eligibilite.setCategorieClient(client.getCategorieClient());
        eligibilite.setEligible(ageValide && permisValide);
        eligibilite.setMotifRefus(motifs.isEmpty() ? null : String.join(" ; ", motifs));

        return eligibilite;
    }

    @Override
    public ClientDTO updateCategorie(Long id, CategorieClient categorie) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client avec l'ID " + id + " non trouvé"));

        client.setCategorieClient(categorie);
        Client updated = clientRepository.save(client);
        return toDTO(updated);
    }

    private ClientDTO toDTO(Client c) {
        ClientDTO dto = new ClientDTO();
        dto.setId(c.getId());
        dto.setCin(c.getCin());
        dto.setNom(c.getNom());
        dto.setPrenom(c.getPrenom());
        dto.setDateNaissance(c.getDateNaissance());
        dto.setEmail(c.getEmail());
        dto.setTelephone(c.getTelephone());
        dto.setAdresse(c.getAdresse());
        dto.setNumeroPermis(c.getNumeroPermis());
        dto.setDateExpirationPermis(c.getDateExpirationPermis());
        dto.setCategorieClient(c.getCategorieClient());
        dto.setDateInscription(c.getDateInscription());

        LocalDate today = LocalDate.now();
        if (c.getDateNaissance() != null) {
            dto.setAge(Period.between(c.getDateNaissance(), today).getYears());
        }
        if (c.getDateExpirationPermis() != null) {
            dto.setPermisValide(c.getDateExpirationPermis().isAfter(today));
        }
        return dto;
    }
}
