package com.driveeasy.vehicule.service;

import com.driveeasy.vehicule.client.LocationServiceClient;
import com.driveeasy.vehicule.dto.VehiculeCreateDTO;
import com.driveeasy.vehicule.dto.VehiculeDTO;
import com.driveeasy.vehicule.exception.DuplicateResourceException;
import com.driveeasy.vehicule.exception.InvalidOperationException;
import com.driveeasy.vehicule.exception.ServiceUnavailableException;
import com.driveeasy.vehicule.model.BoiteVitesse;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.StatutVehicule;
import com.driveeasy.vehicule.repository.InMemoryVehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests unitaires de la logique métier de vehicule-service (sans Spring, dépôt en RAM réel). */
class VehiculeServiceImplTest {

    private LocationServiceClient locationClient;
    private VehiculeServiceImpl service;

    @BeforeEach
    void setUp() {
        locationClient = mock(LocationServiceClient.class);
        service = new VehiculeServiceImpl(new InMemoryVehiculeRepository(), locationClient);
    }

    private VehiculeCreateDTO dto(String immat, String prix) {
        VehiculeCreateDTO d = new VehiculeCreateDTO();
        d.setImmatriculation(immat);
        d.setMarque("Peugeot");
        d.setModele("208");
        d.setAnnee(2023);
        d.setCouleur("Bleu");
        d.setCarburant(Carburant.ESSENCE);
        d.setBoite(BoiteVitesse.MANUELLE);
        d.setNbPlaces(5);
        d.setKilometrage(1000);
        d.setPrixJournalier(new BigDecimal(prix));
        return d;
    }

    @Test
    @DisplayName("Création valide : id généré, immatriculation en majuscules, statut DISPONIBLE")
    void creationValide() {
        VehiculeDTO created = service.createVehicule(dto("100 tun 1", "45.00"));

        assertNotNull(created.getId());
        assertEquals("100 TUN 1", created.getImmatriculation());
        assertEquals(StatutVehicule.DISPONIBLE, created.getStatut());
    }

    @Test
    @DisplayName("Immatriculation en doublon refusée (409), insensible à la casse")
    void doublonImmatriculationRefuse() {
        service.createVehicule(dto("200 TUN 2", "45.00"));

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> service.createVehicule(dto("200 tun 2", "50.00")));
        assertTrue(ex.getMessage().contains("existe déjà"));
    }

    @Test
    @DisplayName("Prix journalier négatif refusé")
    void prixNegatifRefuse() {
        assertThrows(InvalidOperationException.class,
                () -> service.createVehicule(dto("300 TUN 3", "-10.00")));
    }

    @Test
    @DisplayName("Prix journalier nul refusé")
    void prixNulRefuse() {
        assertThrows(InvalidOperationException.class,
                () -> service.createVehicule(dto("301 TUN 3", "0")));
    }

    @Test
    @DisplayName("Année supérieure à l'année courante refusée")
    void anneeFutureRefusee() {
        VehiculeCreateDTO d = dto("400 TUN 4", "45.00");
        d.setAnnee(Year.now().getValue() + 1);
        assertThrows(InvalidOperationException.class, () -> service.createVehicule(d));
    }

    @Test
    @DisplayName("Suppression d'un véhicule LOUE refusée")
    void suppressionVehiculeLoueRefusee() {
        VehiculeCreateDTO d = dto("500 TUN 5", "80.00");
        d.setStatut(StatutVehicule.LOUE);
        VehiculeDTO created = service.createVehicule(d);

        assertThrows(DuplicateResourceException.class, () -> service.deleteVehicule(created.getId()));
    }

    @Test
    @DisplayName("Disponibilité : un véhicule déjà réservé sur la période est exclu")
    void disponibilitesExclutLesVehiculesOccupes() {
        VehiculeDTO libre = service.createVehicule(dto("600 TUN 6", "45.00"));
        VehiculeDTO occupe = service.createVehicule(dto("601 TUN 6", "45.00"));
        when(locationClient.getVehiculesOccupes(any(), any())).thenReturn(List.of(occupe.getId()));

        List<VehiculeDTO> dispo = service.getVehiculesDisponibles(LocalDate.now(), LocalDate.now().plusDays(3));

        assertTrue(dispo.stream().anyMatch(v -> v.getId().equals(libre.getId())));
        assertTrue(dispo.stream().noneMatch(v -> v.getId().equals(occupe.getId())));
    }

    @Test
    @DisplayName("location-service injoignable : 503 plutôt qu'une disponibilité incertaine")
    void disponibilitesServiceLocationIndisponible() {
        service.createVehicule(dto("700 TUN 7", "45.00"));
        when(locationClient.getVehiculesOccupes(any(), any())).thenThrow(new RuntimeException("injoignable"));

        assertThrows(ServiceUnavailableException.class,
                () -> service.getVehiculesDisponibles(LocalDate.now(), LocalDate.now().plusDays(2)));
    }

    @Test
    @DisplayName("Dates incohérentes : début après fin refusé")
    void periodeIncoherente() {
        assertThrows(InvalidOperationException.class,
                () -> service.getVehiculesDisponibles(LocalDate.now().plusDays(5), LocalDate.now()));
    }
}
