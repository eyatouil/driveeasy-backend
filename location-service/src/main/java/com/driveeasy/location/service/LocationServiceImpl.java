package com.driveeasy.location.service;

import com.driveeasy.location.client.ClientServiceClient;
import com.driveeasy.location.client.VehiculeServiceClient;
import com.driveeasy.location.client.dto.*;
import com.driveeasy.location.dto.*;
import com.driveeasy.location.exception.BusinessValidationException;
import com.driveeasy.location.exception.InterServiceException;
import com.driveeasy.location.exception.ResourceNotFoundException;
import com.driveeasy.location.model.Location;
import com.driveeasy.location.model.StatutLocation;
import com.driveeasy.location.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationServiceImpl.class);

    private final LocationRepository locationRepository;
    private final ClientServiceClient clientServiceClient;
    private final VehiculeServiceClient vehiculeServiceClient;

    // Paramètres métier externalisés dans le Config Server (location-service.yml)
    @Value("${driveeasy.location.tarification.remise-fidele:0.05}")
    private BigDecimal remiseFidele;
    @Value("${driveeasy.location.tarification.remise-vip:0.10}")
    private BigDecimal remiseVip;
    @Value("${driveeasy.location.tarification.coefficient-standard:1.0}")
    private BigDecimal coefStandard;
    @Value("${driveeasy.location.tarification.coefficient-fidele:1.0}")
    private BigDecimal coefFidele;
    @Value("${driveeasy.location.tarification.coefficient-vip:1.0}")
    private BigDecimal coefVip;
    @Value("${driveeasy.location.tarification.penalite-retard-facteur:1.5}")
    private BigDecimal facteurPenalite;
    @Value("${driveeasy.location.fidelite.palier-fidele:5}")
    private long palierFidele;
    @Value("${driveeasy.location.fidelite.palier-vip:15}")
    private long palierVip;

    public LocationServiceImpl(LocationRepository locationRepository,
                               ClientServiceClient clientServiceClient,
                               VehiculeServiceClient vehiculeServiceClient) {
        this.locationRepository = locationRepository;
        this.clientServiceClient = clientServiceClient;
        this.vehiculeServiceClient = vehiculeServiceClient;
    }

    @Override
    public List<LocationDTO> getAllLocations(StatutLocation statut, Long clientId, Long vehiculeId, LocalDate dateDebut, LocalDate dateFin) {
        return locationRepository.findWithFilters(statut, clientId, vehiculeId, dateDebut, dateFin).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LocationEnrichieDTO getLocationEnrichie(Long id) {
        Location loc = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location avec l'ID " + id + " non trouvée"));

        ClientResponseDTO client = null;
        try {
            client = clientServiceClient.getClientById(loc.getClientId());
        } catch (Exception e) {
            log.warn("Impossible de récupérer les détails du client ID {}", loc.getClientId(), e);
        }

        VehiculeResponseDTO vehicule = null;
        try {
            vehicule = vehiculeServiceClient.getVehiculeById(loc.getVehiculeId());
        } catch (Exception e) {
            log.warn("Impossible de récupérer les détails du véhicule ID {}", loc.getVehiculeId(), e);
        }

        LocationEnrichieDTO dto = new LocationEnrichieDTO();
        dto.setId(loc.getId());
        dto.setClientId(loc.getClientId());
        dto.setVehiculeId(loc.getVehiculeId());
        dto.setDateDebut(loc.getDateDebut());
        dto.setDateFinPrevue(loc.getDateFinPrevue());
        dto.setDateRetourEffectif(loc.getDateRetourEffectif());
        dto.setKmDepart(loc.getKmDepart());
        dto.setKmRetour(loc.getKmRetour());
        dto.setPrixJournalierApplique(loc.getPrixJournalierApplique());
        dto.setMontantTotal(loc.getMontantTotal());
        dto.setPenalite(loc.getPenalite());
        dto.setStatut(loc.getStatut());
        dto.setDateCreation(loc.getDateCreation());
        dto.setClient(client);
        dto.setVehicule(vehicule);

        long duree = Math.max(1, ChronoUnit.DAYS.between(loc.getDateDebut(), loc.getDateFinPrevue()));
        dto.setDureeJours(duree);

        String catClient = client != null ? client.getCategorieClient() : null;
        BigDecimal remiseRate = tauxRemise(catClient);
        dto.setRemisePourcentage(remiseRate.multiply(new BigDecimal("100")));
        BigDecimal base = loc.getPrixJournalierApplique().multiply(BigDecimal.valueOf(duree)).multiply(coefficient(catClient));
        dto.setRemiseMontant(base.multiply(remiseRate).setScale(2, RoundingMode.HALF_UP));

        return dto;
    }

    @Override
    public LocationDTO createReservation(LocationCreateDTO dto) {
        if (dto.getDateDebut().isAfter(dto.getDateFinPrevue())) {
            throw new BusinessValidationException("La date de début doit être antérieure ou égale à la date de fin");
        }

        // 1. Contrôle de l'éligibilité du client via client-service
        EligibiliteResponseDTO eligibilite;
        try {
            eligibilite = clientServiceClient.getEligibilite(dto.getClientId());
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Client avec l'ID " + dto.getClientId() + " introuvable");
        } catch (Exception e) {
            throw new InterServiceException("Échec de la communication avec client-service : " + e.getMessage(), e);
        }
        if (eligibilite == null || !eligibilite.isEligible()) {
            String motif = eligibilite != null && eligibilite.getMotifRefus() != null
                    ? eligibilite.getMotifRefus()
                    : "Client non éligible pour louer un véhicule";
            throw new BusinessValidationException("Réservation impossible : " + motif);
        }

        // 2. Contrôle du véhicule via vehicule-service
        VehiculeResponseDTO vehicule;
        try {
            vehicule = vehiculeServiceClient.getVehiculeById(dto.getVehiculeId());
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Véhicule avec l'ID " + dto.getVehiculeId() + " introuvable");
        } catch (Exception e) {
            throw new InterServiceException("Échec de la communication avec vehicule-service : " + e.getMessage(), e);
        }

        if (vehicule == null) {
            throw new ResourceNotFoundException("Véhicule avec l'ID " + dto.getVehiculeId() + " introuvable");
        }

        if (!"DISPONIBLE".equalsIgnoreCase(vehicule.getStatut())) {
            throw new BusinessValidationException("Le véhicule " + vehicule.getMarque() + " " + vehicule.getModele() +
                    " n'est pas disponible (statut actuel : " + vehicule.getStatut() + ")");
        }

        // 3. Vérifier les chevauchements de réservation sur la même période
        List<Location> existingVehiculeLocations = locationRepository.findByVehiculeId(dto.getVehiculeId());
        boolean hasOverlap = existingVehiculeLocations.stream()
                .filter(l -> l.getStatut() == StatutLocation.RESERVEE || l.getStatut() == StatutLocation.EN_COURS)
                .anyMatch(l -> datesOverlap(dto.getDateDebut(), dto.getDateFinPrevue(), l.getDateDebut(), l.getDateFinPrevue()));

        if (hasOverlap) {
            throw new BusinessValidationException("Le véhicule est déjà réservé sur cette période");
        }

        // 4. Calcul du prix
        long days = Math.max(1, ChronoUnit.DAYS.between(dto.getDateDebut(), dto.getDateFinPrevue()));
        BigDecimal prixJournalier = vehicule.getPrixJournalier();
        String cat = eligibilite.getCategorieClient();
        // Prix = jours × prix journalier × coefficient catégorie − remise fidélité
        BigDecimal brut = prixJournalier.multiply(BigDecimal.valueOf(days)).multiply(coefficient(cat));
        BigDecimal remiseRate = tauxRemise(cat);
        BigDecimal remiseMontant = brut.multiply(remiseRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montantTotal = brut.subtract(remiseMontant).setScale(2, RoundingMode.HALF_UP);

        // 5. Création et persistance en RAM
        Location location = new Location();
        location.setClientId(dto.getClientId());
        location.setVehiculeId(dto.getVehiculeId());
        location.setDateDebut(dto.getDateDebut());
        location.setDateFinPrevue(dto.getDateFinPrevue());
        location.setKmDepart(vehicule.getKilometrage());
        location.setPrixJournalierApplique(prixJournalier);
        location.setMontantTotal(montantTotal);
        location.setPenalite(BigDecimal.ZERO);
        location.setStatut(StatutLocation.RESERVEE);
        location.setDateCreation(LocalDateTime.now());

        Location saved = locationRepository.save(location);
        return toDTO(saved);
    }

    @Override
    public SimulationResponseDTO simulerPrix(SimulationRequestDTO dto) {
        if (dto.getDateDebut().isAfter(dto.getDateFinPrevue())) {
            throw new BusinessValidationException("La date de début doit être antérieure ou égale à la date de fin");
        }

        VehiculeResponseDTO vehicule;
        try {
            vehicule = vehiculeServiceClient.getVehiculeById(dto.getVehiculeId());
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Véhicule avec l'ID " + dto.getVehiculeId() + " introuvable");
        } catch (Exception e) {
            throw new InterServiceException("Impossible d'obtenir les tarifs du véhicule : " + e.getMessage(), e);
        }

        if (vehicule == null) {
            throw new ResourceNotFoundException("Véhicule introuvable");
        }

        String categorie = dto.getCategorieClient();
        if (dto.getClientId() != null) {
            try {
                ClientResponseDTO client = clientServiceClient.getClientById(dto.getClientId());
                if (client != null && client.getCategorieClient() != null) {
                    categorie = client.getCategorieClient();
                }
            } catch (Exception ignored) {}
        }
        if (categorie == null) {
            categorie = "STANDARD";
        }

        long days = Math.max(1, ChronoUnit.DAYS.between(dto.getDateDebut(), dto.getDateFinPrevue()));
        BigDecimal prixJournalier = vehicule.getPrixJournalier();
        BigDecimal brut = prixJournalier.multiply(BigDecimal.valueOf(days)).multiply(coefficient(categorie)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remiseRate = tauxRemise(categorie);
        BigDecimal remiseMontant = brut.multiply(remiseRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = brut.subtract(remiseMontant).setScale(2, RoundingMode.HALF_UP);

        SimulationResponseDTO resp = new SimulationResponseDTO();
        resp.setVehiculeId(vehicule.getId());
        resp.setVehiculeMarqueModele(vehicule.getMarque() + " " + vehicule.getModele());
        resp.setPrixJournalier(prixJournalier);
        resp.setNombreJours(days);
        resp.setMontantBrut(brut);
        resp.setCategorieClient(categorie);
        resp.setTauxRemise(remiseRate.multiply(new BigDecimal("100")));
        resp.setMontantRemise(remiseMontant);
        resp.setMontantTotalEstime(total);
        return resp;
    }

    @Override
    public LocationDTO demarrerLocation(Long id, DepartRequestDTO dto) {
        Location loc = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location avec l'ID " + id + " non trouvée"));

        if (loc.getStatut() != StatutLocation.RESERVEE) {
            throw new BusinessValidationException("Seule une réservation au statut RESERVEE peut être démarrée (statut actuel : " + loc.getStatut() + ")");
        }

        // Appel inter-service : passage du véhicule au statut LOUE
        try {
            vehiculeServiceClient.updateStatut(loc.getVehiculeId(), new StatutUpdatePayload("LOUE"));
        } catch (Exception e) {
            throw new InterServiceException("Échec de la mise à jour du statut du véhicule : " + e.getMessage(), e);
        }

        if (dto != null && dto.getKmDepart() != null && dto.getKmDepart() > 0) {
            loc.setKmDepart(dto.getKmDepart());
        }
        loc.setStatut(StatutLocation.EN_COURS);

        Location updated = locationRepository.save(loc);
        return toDTO(updated);
    }

    @Override
    public LocationDTO retournerVehicule(Long id, RetourRequestDTO dto) {
        Location loc = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location avec l'ID " + id + " non trouvée"));

        if (loc.getStatut() != StatutLocation.EN_COURS) {
            throw new BusinessValidationException("Seule une location EN_COURS peut être clôturée (statut actuel : " + loc.getStatut() + ")");
        }

        if (dto.getKmRetour() < loc.getKmDepart()) {
            throw new BusinessValidationException("Le kilométrage au retour (" + dto.getKmRetour() +
                    ") ne peut être inférieur au kilométrage de départ (" + loc.getKmDepart() + ")");
        }

        LocalDate dateRetour = dto.getDateRetourEffectif() != null ? dto.getDateRetourEffectif() : LocalDate.now();

        // Pénalité de retard : 1,5 × prix journalier par jour de retard
        BigDecimal penalite = BigDecimal.ZERO;
        if (dateRetour.isAfter(loc.getDateFinPrevue())) {
            long joursRetard = ChronoUnit.DAYS.between(loc.getDateFinPrevue(), dateRetour);
            penalite = facteurPenalite.multiply(loc.getPrixJournalierApplique()).multiply(BigDecimal.valueOf(joursRetard))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Appel inter-service en premier : si le véhicule ne peut pas être libéré, la location
        // reste EN_COURS et rien n'est modifié (cohérence de la transaction distribuée).
        try {
            vehiculeServiceClient.updateStatut(loc.getVehiculeId(), new StatutUpdatePayload("DISPONIBLE", dto.getKmRetour()));
        } catch (Exception e) {
            log.error("Retour annulé : échec de la mise à jour du véhicule {} : {}", loc.getVehiculeId(), e.getMessage());
            throw new InterServiceException("Retour impossible : échec de la mise à jour du véhicule : " + e.getMessage(), e);
        }

        loc.setDateRetourEffectif(dateRetour);
        loc.setKmRetour(dto.getKmRetour());
        loc.setPenalite(penalite);
        loc.setMontantTotal(loc.getMontantTotal().add(penalite));
        loc.setStatut(StatutLocation.TERMINEE);

        Location updated = locationRepository.save(loc);

        // Mise à jour automatique de la fidélité client :
        // 5 locations terminées -> FIDELE, 15 -> VIP
        try {
            long countTerminees = locationRepository.countCompletedByClientId(loc.getClientId());
            if (countTerminees >= palierVip) {
                clientServiceClient.updateCategorie(loc.getClientId(), new CategorieUpdatePayload("VIP"));
            } else if (countTerminees >= palierFidele) {
                clientServiceClient.updateCategorie(loc.getClientId(), new CategorieUpdatePayload("FIDELE"));
            }
        } catch (Exception e) {
            log.warn("Impossible de mettre à jour la catégorie de fidélité du client : {}", e.getMessage());
        }

        return toDTO(updated);
    }

    @Override
    public LocationDTO annulerLocation(Long id) {
        Location loc = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location avec l'ID " + id + " non trouvée"));

        if (loc.getStatut() == StatutLocation.TERMINEE || loc.getStatut() == StatutLocation.ANNULEE) {
            throw new BusinessValidationException("Cette location ne peut plus être annulée (statut : " + loc.getStatut() + ")");
        }

        // Si la location était en cours, libérer le véhicule
        if (loc.getStatut() == StatutLocation.EN_COURS) {
            try {
                vehiculeServiceClient.updateStatut(loc.getVehiculeId(), new StatutUpdatePayload("DISPONIBLE"));
            } catch (Exception e) {
                log.error("Annulation refusée : véhicule {} non libéré : {}", loc.getVehiculeId(), e.getMessage());
                throw new InterServiceException("Annulation impossible : échec de la libération du véhicule : " + e.getMessage(), e);
            }
        }

        loc.setStatut(StatutLocation.ANNULEE);
        Location updated = locationRepository.save(loc);
        return toDTO(updated);
    }

    @Override
    public StatistiquesDTO getStatistiques() {
        List<Location> all = locationRepository.findAll();

        BigDecimal caTotal = all.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE || l.getStatut() == StatutLocation.EN_COURS)
                .map(Location::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth currentMonth = YearMonth.now();
        BigDecimal caMois = all.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE || l.getStatut() == StatutLocation.EN_COURS)
                .filter(l -> l.getDateDebut() != null && YearMonth.from(l.getDateDebut()).equals(currentMonth))
                .map(Location::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long reservees = locationRepository.countByStatut(StatutLocation.RESERVEE);
        long enCours = locationRepository.countByStatut(StatutLocation.EN_COURS);
        long terminees = locationRepository.countByStatut(StatutLocation.TERMINEE);
        long annulees = locationRepository.countByStatut(StatutLocation.ANNULEE);
        long total = all.size();

        // Taux d'occupation = locations EN_COURS / taille du parc (tous statuts confondus)
        double tauxOccupation = 0.0;
        try {
            List<VehiculeResponseDTO> parc = vehiculeServiceClient.getTousVehicules();
            if (parc != null && !parc.isEmpty()) {
                tauxOccupation = Math.round(((double) enCours / parc.size() * 100.0) * 10.0) / 10.0;
            }
        } catch (Exception e) {
            log.warn("Taux d'occupation indisponible (vehicule-service injoignable) : {}", e.getMessage());
        }

        // Top véhicules les plus loués
        Map<Long, Long> countsByVehicule = all.stream()
                .filter(l -> l.getStatut() != StatutLocation.ANNULEE)
                .collect(Collectors.groupingBy(Location::getVehiculeId, Collectors.counting()));

        List<TopVehiculeDTO> topVehicules = countsByVehicule.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    String marqueModele = "Véhicule #" + entry.getKey();
                    String immat = "";
                    try {
                        VehiculeResponseDTO v = vehiculeServiceClient.getVehiculeById(entry.getKey());
                        if (v != null) {
                            marqueModele = v.getMarque() + " " + v.getModele();
                            immat = v.getImmatriculation();
                        }
                    } catch (Exception ignored) {}
                    return new TopVehiculeDTO(entry.getKey(), immat, marqueModele, entry.getValue());
                })
                .collect(Collectors.toList());

        StatistiquesDTO stats = new StatistiquesDTO();
        stats.setChiffreAffairesTotal(caTotal);
        stats.setChiffreAffairesMoisCourant(caMois);
        stats.setTauxOccupation(tauxOccupation);
        stats.setTotalLocations(total);
        stats.setLocationsEnCours(enCours);
        stats.setLocationsTerminees(terminees);
        stats.setLocationsReservees(reservees);
        stats.setLocationsAnnulees(annulees);
        stats.setTopVehicules(topVehicules);

        return stats;
    }

    /** Remise de fidélité selon la catégorie (FIDELE : 5 %, VIP : 10 %). */
    private BigDecimal tauxRemise(String categorie) {
        if ("VIP".equalsIgnoreCase(categorie)) return remiseVip;
        if ("FIDELE".equalsIgnoreCase(categorie)) return remiseFidele;
        return BigDecimal.ZERO;
    }

    /** Coefficient de tarification selon la catégorie du client. */
    private BigDecimal coefficient(String categorie) {
        if ("VIP".equalsIgnoreCase(categorie)) return coefVip;
        if ("FIDELE".equalsIgnoreCase(categorie)) return coefFidele;
        return coefStandard;
    }

    @Override
    public PageResult<LocationDTO> getLocationsPage(StatutLocation statut, Long clientId, Long vehiculeId,
                                                    LocalDate dateDebut, LocalDate dateFin, int page, int size) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int safePage = Math.max(0, page);
        List<LocationDTO> all = getAllLocations(statut, clientId, vehiculeId, dateDebut, dateFin);
        int from = (int) Math.min((long) safePage * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(new ArrayList<>(all.subList(from, to)), safePage, safeSize, all.size());
    }

    @Override
    public List<Long> getVehiculesOccupes(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut.isAfter(dateFin)) {
            throw new BusinessValidationException("La date de début doit être antérieure ou égale à la date de fin");
        }
        return locationRepository.findAll().stream()
                .filter(l -> l.getStatut() == StatutLocation.RESERVEE || l.getStatut() == StatutLocation.EN_COURS)
                .filter(l -> datesOverlap(dateDebut, dateFin, l.getDateDebut(), l.getDateFinPrevue()))
                .map(Location::getVehiculeId)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    private LocationDTO toDTO(Location l) {
        LocationDTO dto = new LocationDTO();
        dto.setId(l.getId());
        dto.setClientId(l.getClientId());
        dto.setVehiculeId(l.getVehiculeId());
        dto.setDateDebut(l.getDateDebut());
        dto.setDateFinPrevue(l.getDateFinPrevue());
        dto.setDateRetourEffectif(l.getDateRetourEffectif());
        dto.setKmDepart(l.getKmDepart());
        dto.setKmRetour(l.getKmRetour());
        dto.setPrixJournalierApplique(l.getPrixJournalierApplique());
        dto.setMontantTotal(l.getMontantTotal());
        dto.setPenalite(l.getPenalite());
        dto.setStatut(l.getStatut());
        dto.setDateCreation(l.getDateCreation());
        return dto;
    }
}
