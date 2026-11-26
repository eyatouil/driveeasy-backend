package com.driveeasy.location.service;

import com.driveeasy.location.client.ClientServiceClient;
import com.driveeasy.location.client.VehiculeServiceClient;
import com.driveeasy.location.client.dto.CategorieUpdatePayload;
import com.driveeasy.location.client.dto.EligibiliteResponseDTO;
import com.driveeasy.location.client.dto.StatutUpdatePayload;
import com.driveeasy.location.client.dto.VehiculeResponseDTO;
import com.driveeasy.location.dto.LocationCreateDTO;
import com.driveeasy.location.dto.LocationDTO;
import com.driveeasy.location.dto.RetourRequestDTO;
import com.driveeasy.location.dto.SimulationRequestDTO;
import com.driveeasy.location.dto.SimulationResponseDTO;
import com.driveeasy.location.exception.BusinessValidationException;
import com.driveeasy.location.exception.InterServiceException;
import com.driveeasy.location.model.Location;
import com.driveeasy.location.model.StatutLocation;
import com.driveeasy.location.repository.InMemoryLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de location-service : tarification, remises fidélité, pénalités de retard,
 * validations inter-services. Les autres services sont simulés (Mockito), le dépôt RAM est réel.
 */
class LocationServiceImplTest {

    private static final Long CLIENT_ID = 1L;
    private static final Long VEHICULE_ID = 10L;

    private ClientServiceClient clientClient;
    private VehiculeServiceClient vehiculeClient;
    private InMemoryLocationRepository repo;
    private LocationServiceImpl service;

    @BeforeEach
    void setUp() {
        clientClient = mock(ClientServiceClient.class);
        vehiculeClient = mock(VehiculeServiceClient.class);
        repo = new InMemoryLocationRepository();
        service = new LocationServiceImpl(repo, clientClient, vehiculeClient);

        // Les @Value ne sont pas injectés sans contexte Spring : on reproduit location-service.yml
        ReflectionTestUtils.setField(service, "remiseFidele", new BigDecimal("0.05"));
        ReflectionTestUtils.setField(service, "remiseVip", new BigDecimal("0.10"));
        ReflectionTestUtils.setField(service, "coefStandard", BigDecimal.ONE);
        ReflectionTestUtils.setField(service, "coefFidele", BigDecimal.ONE);
        ReflectionTestUtils.setField(service, "coefVip", BigDecimal.ONE);
        ReflectionTestUtils.setField(service, "facteurPenalite", new BigDecimal("1.5"));
        ReflectionTestUtils.setField(service, "palierFidele", 5L);
        ReflectionTestUtils.setField(service, "palierVip", 15L);
    }

    // ---------- helpers ----------

    private void vehiculeDisponible(String prixJournalier) {
        VehiculeResponseDTO v = new VehiculeResponseDTO();
        v.setId(VEHICULE_ID);
        v.setMarque("Peugeot");
        v.setModele("208");
        v.setStatut("DISPONIBLE");
        v.setPrixJournalier(new BigDecimal(prixJournalier));
        v.setKilometrage(20000);
        when(vehiculeClient.getVehiculeById(VEHICULE_ID)).thenReturn(v);
    }

    private void clientEligible(String categorie) {
        EligibiliteResponseDTO e = new EligibiliteResponseDTO();
        e.setClientId(CLIENT_ID);
        e.setEligible(true);
        e.setCategorieClient(categorie);
        when(clientClient.getEligibilite(CLIENT_ID)).thenReturn(e);
    }

    private LocationDTO reserver(LocalDate debut, LocalDate fin) {
        LocationCreateDTO dto = new LocationCreateDTO();
        dto.setClientId(CLIENT_ID);
        dto.setVehiculeId(VEHICULE_ID);
        dto.setDateDebut(debut);
        dto.setDateFinPrevue(fin);
        return service.createReservation(dto);
    }

    private SimulationRequestDTO simulation(String categorie, LocalDate debut, LocalDate fin) {
        SimulationRequestDTO dto = new SimulationRequestDTO();
        dto.setVehiculeId(VEHICULE_ID);
        dto.setCategorieClient(categorie);
        dto.setDateDebut(debut);
        dto.setDateFinPrevue(fin);
        return dto;
    }

    private RetourRequestDTO retour(int km, LocalDate date) {
        RetourRequestDTO r = new RetourRequestDTO();
        r.setKmRetour(km);
        r.setDateRetourEffectif(date);
        return r;
    }

    private void assertMontant(String attendu, BigDecimal reel) {
        assertNotNull(reel, "montant null");
        assertEquals(0, new BigDecimal(attendu).compareTo(reel),
                "attendu " + attendu + " mais obtenu " + reel);
    }

    // ---------- simulation tarifaire ----------

    @Test
    @DisplayName("Simulation VIP : 3 jours x 100 = 300, remise 10 % = 30, total 270")
    void simulationVip() {
        vehiculeDisponible("100.00");
        LocalDate d = LocalDate.now();

        SimulationResponseDTO r = service.simulerPrix(simulation("VIP", d, d.plusDays(3)));

        assertMontant("300.00", r.getMontantBrut());
        assertMontant("30.00", r.getMontantRemise());
        assertMontant("270.00", r.getMontantTotalEstime());
    }

    @Test
    @DisplayName("Simulation FIDELE : remise de 5 %")
    void simulationFidele() {
        vehiculeDisponible("100.00");
        LocalDate d = LocalDate.now();

        assertMontant("285.00", service.simulerPrix(simulation("FIDELE", d, d.plusDays(3))).getMontantTotalEstime());
    }

    @Test
    @DisplayName("Simulation sans catégorie : STANDARD, aucune remise")
    void simulationStandard() {
        vehiculeDisponible("100.00");
        LocalDate d = LocalDate.now();

        SimulationResponseDTO r = service.simulerPrix(simulation(null, d, d.plusDays(3)));

        assertEquals("STANDARD", r.getCategorieClient());
        assertMontant("300.00", r.getMontantTotalEstime());
    }

    @Test
    @DisplayName("Simulation sur une seule journée (début = fin) : facturée 1 jour minimum")
    void simulationMinimumUnJour() {
        vehiculeDisponible("100.00");
        LocalDate d = LocalDate.now();

        assertMontant("100.00", service.simulerPrix(simulation("STANDARD", d, d)).getMontantTotalEstime());
    }

    @Test
    @DisplayName("Simulation : date de début après la date de fin refusée")
    void simulationDatesInversees() {
        vehiculeDisponible("100.00");
        LocalDate d = LocalDate.now();

        assertThrows(BusinessValidationException.class,
                () -> service.simulerPrix(simulation("VIP", d.plusDays(5), d)));
    }

    // ---------- réservation ----------

    @Test
    @DisplayName("Réservation VIP : montant avec remise, statut RESERVEE, pénalité nulle")
    void reservationVip() {
        vehiculeDisponible("100.00");
        clientEligible("VIP");
        LocalDate d = LocalDate.now().plusDays(1);

        LocationDTO loc = reserver(d, d.plusDays(3));

        assertNotNull(loc.getId());
        assertEquals(StatutLocation.RESERVEE, loc.getStatut());
        assertMontant("270.00", loc.getMontantTotal());
        assertMontant("0", loc.getPenalite());
    }

    @Test
    @DisplayName("Client inéligible : réservation refusée avant même d'interroger vehicule-service")
    void reservationClientIneligible() {
        EligibiliteResponseDTO e = new EligibiliteResponseDTO();
        e.setEligible(false);
        e.setMotifRefus("Âge insuffisant : 19 ans (minimum requis : 21 ans)");
        when(clientClient.getEligibilite(CLIENT_ID)).thenReturn(e);
        LocalDate d = LocalDate.now().plusDays(1);

        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> reserver(d, d.plusDays(2)));

        assertTrue(ex.getMessage().contains("Âge insuffisant"));
        verify(vehiculeClient, never()).getVehiculeById(anyLong());
    }

    @Test
    @DisplayName("Véhicule non DISPONIBLE : réservation refusée")
    void reservationVehiculeIndisponible() {
        clientEligible("STANDARD");
        VehiculeResponseDTO v = new VehiculeResponseDTO();
        v.setId(VEHICULE_ID);
        v.setMarque("Dacia");
        v.setModele("Sandero");
        v.setStatut("MAINTENANCE");
        v.setPrixJournalier(new BigDecimal("35.00"));
        when(vehiculeClient.getVehiculeById(VEHICULE_ID)).thenReturn(v);
        LocalDate d = LocalDate.now().plusDays(1);

        assertThrows(BusinessValidationException.class, () -> reserver(d, d.plusDays(2)));
    }

    @Test
    @DisplayName("Chevauchement de période sur le même véhicule refusé, période suivante acceptée")
    void reservationChevauchement() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocalDate d = LocalDate.now().plusDays(1);
        reserver(d, d.plusDays(3));

        assertThrows(BusinessValidationException.class, () -> reserver(d.plusDays(2), d.plusDays(5)));
        assertDoesNotThrow(() -> reserver(d.plusDays(4), d.plusDays(6)));
    }

    @Test
    @DisplayName("Réservation : date de début après la date de fin refusée")
    void reservationDatesInversees() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocalDate d = LocalDate.now().plusDays(5);

        assertThrows(BusinessValidationException.class, () -> reserver(d, d.minusDays(2)));
    }

    @Test
    @DisplayName("client-service injoignable : erreur inter-service (503)")
    void reservationClientServiceInjoignable() {
        when(clientClient.getEligibilite(CLIENT_ID)).thenThrow(new RuntimeException("connexion refusée"));
        LocalDate d = LocalDate.now().plusDays(1);

        assertThrows(InterServiceException.class, () -> reserver(d, d.plusDays(2)));
    }

    // ---------- départ ----------

    @Test
    @DisplayName("Départ : la location passe EN_COURS et le véhicule est mis à LOUE")
    void depart() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocalDate d = LocalDate.now();
        LocationDTO loc = reserver(d, d.plusDays(3));

        LocationDTO en = service.demarrerLocation(loc.getId(), null);

        assertEquals(StatutLocation.EN_COURS, en.getStatut());
        ArgumentCaptor<StatutUpdatePayload> captor = ArgumentCaptor.forClass(StatutUpdatePayload.class);
        verify(vehiculeClient).updateStatut(eq(VEHICULE_ID), captor.capture());
        assertEquals("LOUE", captor.getValue().getStatut());
    }

    @Test
    @DisplayName("Départ impossible deux fois (la location n'est plus RESERVEE)")
    void departDeuxFois() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocalDate d = LocalDate.now();
        LocationDTO loc = reserver(d, d.plusDays(3));
        service.demarrerLocation(loc.getId(), null);

        assertThrows(BusinessValidationException.class, () -> service.demarrerLocation(loc.getId(), null));
    }

    // ---------- retour et pénalités ----------

    /** Réserve (3 jours finissant il y a 2 jours) puis démarre : retourne la location EN_COURS. */
    private LocationDTO locationEnCours() {
        LocalDate debut = LocalDate.now().minusDays(5);
        LocalDate fin = LocalDate.now().minusDays(2);
        LocationDTO loc = reserver(debut, fin);
        return service.demarrerLocation(loc.getId(), null);
    }

    @Test
    @DisplayName("Retour avec 2 jours de retard : pénalité = 2 x 1,5 x 50 = 150, total = 150 + 150 = 300")
    void retourAvecRetard() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocationDTO en = locationEnCours();
        LocalDate retourEffectif = LocalDate.now().minusDays(2).plusDays(2);

        LocationDTO fin = service.retournerVehicule(en.getId(), retour(20500, retourEffectif));

        assertEquals(StatutLocation.TERMINEE, fin.getStatut());
        assertMontant("150.00", fin.getPenalite());
        assertMontant("300.00", fin.getMontantTotal());
        assertEquals(20500, fin.getKmRetour());
    }

    @Test
    @DisplayName("Retour à la date prévue : aucune pénalité")
    void retourSansRetard() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocationDTO en = locationEnCours();

        LocationDTO fin = service.retournerVehicule(en.getId(), retour(20300, LocalDate.now().minusDays(2)));

        assertMontant("0", fin.getPenalite());
        assertMontant("150.00", fin.getMontantTotal());
    }

    @Test
    @DisplayName("Kilométrage de retour inférieur au départ refusé")
    void retourKilometrageIncoherent() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocationDTO en = locationEnCours();

        assertThrows(BusinessValidationException.class,
                () -> service.retournerVehicule(en.getId(), retour(100, LocalDate.now())));
    }

    @Test
    @DisplayName("Retour d'une location non EN_COURS refusé")
    void retourSurReservation() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocalDate d = LocalDate.now().plusDays(1);
        LocationDTO loc = reserver(d, d.plusDays(2));

        assertThrows(BusinessValidationException.class,
                () -> service.retournerVehicule(loc.getId(), retour(21000, LocalDate.now())));
    }

    @Test
    @DisplayName("Échec de vehicule-service au retour : la location reste EN_COURS (cohérence)")
    void retourVehiculeServiceEnPanne() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocationDTO en = locationEnCours();
        doThrow(new RuntimeException("vehicule-service down"))
                .when(vehiculeClient).updateStatut(anyLong(), any(StatutUpdatePayload.class));

        assertThrows(InterServiceException.class,
                () -> service.retournerVehicule(en.getId(), retour(20300, LocalDate.now())));

        assertEquals(StatutLocation.EN_COURS, repo.findById(en.getId()).orElseThrow().getStatut());
    }

    // ---------- fidélité automatique ----------

    private void ajouterLocationsTerminees(int nombre) {
        for (int i = 0; i < nombre; i++) {
            Location l = new Location();
            l.setClientId(CLIENT_ID);
            l.setVehiculeId(99L);
            l.setStatut(StatutLocation.TERMINEE);
            repo.save(l);
        }
    }

    @Test
    @DisplayName("5e location terminée : le client passe FIDELE")
    void promotionFidele() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        ajouterLocationsTerminees(4);
        LocationDTO en = locationEnCours();

        service.retournerVehicule(en.getId(), retour(20300, LocalDate.now().minusDays(2)));

        ArgumentCaptor<CategorieUpdatePayload> captor = ArgumentCaptor.forClass(CategorieUpdatePayload.class);
        verify(clientClient).updateCategorie(eq(CLIENT_ID), captor.capture());
        assertEquals("FIDELE", captor.getValue().getCategorie());
    }

    @Test
    @DisplayName("15e location terminée : le client passe VIP")
    void promotionVip() {
        vehiculeDisponible("50.00");
        clientEligible("FIDELE");
        ajouterLocationsTerminees(14);
        LocationDTO en = locationEnCours();

        service.retournerVehicule(en.getId(), retour(20300, LocalDate.now().minusDays(2)));

        ArgumentCaptor<CategorieUpdatePayload> captor = ArgumentCaptor.forClass(CategorieUpdatePayload.class);
        verify(clientClient).updateCategorie(eq(CLIENT_ID), captor.capture());
        assertEquals("VIP", captor.getValue().getCategorie());
    }

    @Test
    @DisplayName("Moins de 5 locations terminées : aucune promotion")
    void pasDePromotion() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        ajouterLocationsTerminees(2);
        LocationDTO en = locationEnCours();

        service.retournerVehicule(en.getId(), retour(20300, LocalDate.now().minusDays(2)));

        verify(clientClient, never()).updateCategorie(anyLong(), any(CategorieUpdatePayload.class));
    }

    // ---------- annulation ----------

    @Test
    @DisplayName("Annulation d'une réservation : statut ANNULEE")
    void annulationReservation() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocalDate d = LocalDate.now().plusDays(1);
        LocationDTO loc = reserver(d, d.plusDays(2));

        assertEquals(StatutLocation.ANNULEE, service.annulerLocation(loc.getId()).getStatut());
    }

    @Test
    @DisplayName("Une location déjà annulée ne peut plus l'être")
    void doubleAnnulation() {
        vehiculeDisponible("50.00");
        clientEligible("STANDARD");
        LocalDate d = LocalDate.now().plusDays(1);
        LocationDTO loc = reserver(d, d.plusDays(2));
        service.annulerLocation(loc.getId());

        assertThrows(BusinessValidationException.class, () -> service.annulerLocation(loc.getId()));
    }
}
