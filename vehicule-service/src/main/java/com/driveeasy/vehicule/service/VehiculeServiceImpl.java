package com.driveeasy.vehicule.service;

import com.driveeasy.vehicule.dto.StatutUpdateDTO;
import com.driveeasy.vehicule.dto.VehiculeCreateDTO;
import com.driveeasy.vehicule.dto.VehiculeDTO;
import com.driveeasy.vehicule.dto.VehiculeUpdateDTO;
import com.driveeasy.vehicule.exception.DuplicateResourceException;
import com.driveeasy.vehicule.exception.InvalidOperationException;
import com.driveeasy.vehicule.exception.ResourceNotFoundException;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.StatutVehicule;
import com.driveeasy.vehicule.model.Vehicule;
import com.driveeasy.vehicule.repository.VehiculeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;

    public VehiculeServiceImpl(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }

    @Override
    public List<VehiculeDTO> getAllVehicules(String query, Carburant carburant, StatutVehicule statut, BigDecimal prixMax) {
        return vehiculeRepository.findWithFilters(query, carburant, statut, prixMax).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VehiculeDTO getVehiculeById(Long id) {
        Vehicule v = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule avec l'ID " + id + " non trouvé"));
        return toDTO(v);
    }

    @Override
    public VehiculeDTO createVehicule(VehiculeCreateDTO dto) {
        int currentYear = Year.now().getValue();
        if (dto.getAnnee() > currentYear + 1) {
            throw new InvalidOperationException("L'année du véhicule ne peut pas dépasser " + (currentYear + 1));
        }

        if (dto.getPrixJournalier() == null || dto.getPrixJournalier().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Le prix journalier doit être strictement positif");
        }

        if (vehiculeRepository.existsByImmatriculation(dto.getImmatriculation())) {
            throw new DuplicateResourceException("Un véhicule avec l'immatriculation '" + dto.getImmatriculation() + "' existe déjà");
        }

        Vehicule vehicule = new Vehicule();
        vehicule.setImmatriculation(dto.getImmatriculation().trim().toUpperCase());
        vehicule.setMarque(dto.getMarque().trim());
        vehicule.setModele(dto.getModele().trim());
        vehicule.setAnnee(dto.getAnnee());
        vehicule.setCouleur(dto.getCouleur().trim());
        vehicule.setCarburant(dto.getCarburant());
        vehicule.setBoite(dto.getBoite());
        vehicule.setNbPlaces(dto.getNbPlaces());
        vehicule.setKilometrage(dto.getKilometrage());
        vehicule.setPrixJournalier(dto.getPrixJournalier());
        vehicule.setStatut(dto.getStatut() != null ? dto.getStatut() : StatutVehicule.DISPONIBLE);
        vehicule.setImageUrl(dto.getImageUrl());

        Vehicule saved = vehiculeRepository.save(vehicule);
        return toDTO(saved);
    }

    @Override
    public VehiculeDTO updateVehicule(Long id, VehiculeUpdateDTO dto) {
        Vehicule v = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule avec l'ID " + id + " non trouvé"));

        if (dto.getMarque() != null && !dto.getMarque().isBlank()) v.setMarque(dto.getMarque().trim());
        if (dto.getModele() != null && !dto.getModele().isBlank()) v.setModele(dto.getModele().trim());
        if (dto.getAnnee() != null) {
            int currentYear = Year.now().getValue();
            if (dto.getAnnee() > currentYear + 1) {
                throw new InvalidOperationException("L'année du véhicule ne peut pas dépasser " + (currentYear + 1));
            }
            v.setAnnee(dto.getAnnee());
        }
        if (dto.getCouleur() != null && !dto.getCouleur().isBlank()) v.setCouleur(dto.getCouleur().trim());
        if (dto.getCarburant() != null) v.setCarburant(dto.getCarburant());
        if (dto.getBoite() != null) v.setBoite(dto.getBoite());
        if (dto.getNbPlaces() != null) v.setNbPlaces(dto.getNbPlaces());
        if (dto.getKilometrage() != null) v.setKilometrage(dto.getKilometrage());
        if (dto.getPrixJournalier() != null) {
            if (dto.getPrixJournalier().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidOperationException("Le prix journalier doit être strictement positif");
            }
            v.setPrixJournalier(dto.getPrixJournalier());
        }
        if (dto.getStatut() != null) v.setStatut(dto.getStatut());
        if (dto.getImageUrl() != null) v.setImageUrl(dto.getImageUrl());

        Vehicule updated = vehiculeRepository.save(v);
        return toDTO(updated);
    }

    @Override
    public void deleteVehicule(Long id) {
        Vehicule v = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule avec l'ID " + id + " non trouvé"));

        if (v.getStatut() == StatutVehicule.LOUE) {
            throw new DuplicateResourceException("Impossible de supprimer un véhicule actuellement LOUÉ");
        }

        vehiculeRepository.deleteById(id);
    }

    @Override
    public VehiculeDTO updateStatut(Long id, StatutUpdateDTO statutDTO) {
        Vehicule v = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule avec l'ID " + id + " non trouvé"));

        v.setStatut(statutDTO.getStatut());
        if (statutDTO.getKilometrage() != null && statutDTO.getKilometrage() >= v.getKilometrage()) {
            v.setKilometrage(statutDTO.getKilometrage());
        }

        Vehicule updated = vehiculeRepository.save(v);
        return toDTO(updated);
    }

    @Override
    public List<VehiculeDTO> getVehiculesDisponibles(LocalDate dateDebut, LocalDate dateFin) {
        // En mémoire, un véhicule disponible est un véhicule avec le statut DISPONIBLE
        return vehiculeRepository.findDisponibles().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private VehiculeDTO toDTO(Vehicule v) {
        VehiculeDTO dto = new VehiculeDTO();
        dto.setId(v.getId());
        dto.setImmatriculation(v.getImmatriculation());
        dto.setMarque(v.getMarque());
        dto.setModele(v.getModele());
        dto.setAnnee(v.getAnnee());
        dto.setCouleur(v.getCouleur());
        dto.setCarburant(v.getCarburant());
        dto.setBoite(v.getBoite());
        dto.setNbPlaces(v.getNbPlaces());
        dto.setKilometrage(v.getKilometrage());
        dto.setPrixJournalier(v.getPrixJournalier());
        dto.setStatut(v.getStatut());
        dto.setImageUrl(v.getImageUrl());
        return dto;
    }
}
