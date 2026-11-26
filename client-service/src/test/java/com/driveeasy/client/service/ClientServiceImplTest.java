package com.driveeasy.client.service;

import com.driveeasy.client.client.LocationServiceClient;
import com.driveeasy.client.dto.ClientCreateDTO;
import com.driveeasy.client.dto.ClientDTO;
import com.driveeasy.client.dto.EligibiliteDTO;
import com.driveeasy.client.exception.DuplicateResourceException;
import com.driveeasy.client.model.CategorieClient;
import com.driveeasy.client.repository.InMemoryClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests unitaires de client-service : éligibilité, unicité, suppression. */
class ClientServiceImplTest {

    private LocationServiceClient locationClient;
    private ClientServiceImpl service;

    @BeforeEach
    void setUp() {
        locationClient = mock(LocationServiceClient.class);
        service = new ClientServiceImpl(new InMemoryClientRepository(), locationClient);
    }

    private ClientCreateDTO dto(String cin, String email, String permis, LocalDate naissance, LocalDate expiration) {
        ClientCreateDTO d = new ClientCreateDTO();
        d.setCin(cin);
        d.setNom("Test");
        d.setPrenom("Client");
        d.setDateNaissance(naissance);
        d.setEmail(email);
        d.setTelephone("+216 20 000 000");
        d.setAdresse("Tunis");
        d.setNumeroPermis(permis);
        d.setDateExpirationPermis(expiration);
        return d;
    }

    private ClientDTO clientValide(String suffix) {
        return service.createClient(dto("CIN" + suffix, suffix + "@test.tn", "P" + suffix,
                LocalDate.now().minusYears(30), LocalDate.now().plusYears(2)));
    }

    @Test
    @DisplayName("Client majeur avec permis valide : éligible")
    void clientEligible() {
        ClientDTO c = clientValide("1");

        EligibiliteDTO e = service.checkEligibilite(c.getId());

        assertTrue(e.isEligible());
        assertNull(e.getMotifRefus());
    }

    @Test
    @DisplayName("Client de 19 ans : inéligible (âge < 21)")
    void clientTropJeune() {
        ClientDTO c = service.createClient(dto("CIN2", "jeune@test.tn", "P2",
                LocalDate.now().minusYears(19), LocalDate.now().plusYears(2)));

        EligibiliteDTO e = service.checkEligibilite(c.getId());

        assertFalse(e.isEligible());
        assertFalse(e.isAgeValide());
        assertTrue(e.isPermisValide());
        assertNotNull(e.getMotifRefus());
    }

    @Test
    @DisplayName("Client de 21 ans pile : éligible (limite incluse)")
    void clientExactement21Ans() {
        ClientDTO c = service.createClient(dto("CIN3", "limite@test.tn", "P3",
                LocalDate.now().minusYears(21), LocalDate.now().plusYears(2)));

        assertTrue(service.checkEligibilite(c.getId()).isAgeValide());
    }

    @Test
    @DisplayName("Permis expiré : inéligible")
    void permisExpire() {
        ClientDTO c = service.createClient(dto("CIN4", "expire@test.tn", "P4",
                LocalDate.now().minusYears(30), LocalDate.now().minusMonths(1)));

        EligibiliteDTO e = service.checkEligibilite(c.getId());

        assertFalse(e.isEligible());
        assertTrue(e.isAgeValide());
        assertFalse(e.isPermisValide());
    }

    @Test
    @DisplayName("Permis expirant aujourd'hui : inéligible (doit être strictement après aujourd'hui)")
    void permisExpireAujourdhui() {
        ClientDTO c = service.createClient(dto("CIN5", "today@test.tn", "P5",
                LocalDate.now().minusYears(30), LocalDate.now()));

        assertFalse(service.checkEligibilite(c.getId()).isPermisValide());
    }

    @Test
    @DisplayName("Âge insuffisant ET permis expiré : les deux motifs sont signalés")
    void deuxMotifs() {
        ClientDTO c = service.createClient(dto("CIN6", "double@test.tn", "P6",
                LocalDate.now().minusYears(18), LocalDate.now().minusDays(1)));

        EligibiliteDTO e = service.checkEligibilite(c.getId());

        assertFalse(e.isEligible());
        assertTrue(e.getMotifRefus().contains("Âge insuffisant"));
        assertTrue(e.getMotifRefus().contains("Permis"));
    }

    @Test
    @DisplayName("CIN en doublon refusé (409)")
    void cinEnDoublon() {
        clientValide("7");

        assertThrows(DuplicateResourceException.class, () -> service.createClient(dto("CIN7", "autre@test.tn", "PX",
                LocalDate.now().minusYears(30), LocalDate.now().plusYears(2))));
    }

    @Test
    @DisplayName("Email en doublon refusé (409)")
    void emailEnDoublon() {
        clientValide("8");

        assertThrows(DuplicateResourceException.class, () -> service.createClient(dto("AUTRECIN", "8@test.tn", "PY",
                LocalDate.now().minusYears(30), LocalDate.now().plusYears(2))));
    }

    @Test
    @DisplayName("Suppression refusée si le client a une location active")
    void suppressionAvecLocationActive() {
        ClientDTO c = clientValide("9");
        when(locationClient.hasActiveLocation(c.getId())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.deleteClient(c.getId()));
        assertNotNull(service.getClientById(c.getId()));
    }

    @Test
    @DisplayName("Suppression autorisée sans location active")
    void suppressionSansLocationActive() {
        ClientDTO c = clientValide("10");
        when(locationClient.hasActiveLocation(c.getId())).thenReturn(false);

        service.deleteClient(c.getId());

        assertThrows(RuntimeException.class, () -> service.getClientById(c.getId()));
    }

    @Test
    @DisplayName("Changement de catégorie de fidélité")
    void changementCategorie() {
        ClientDTO c = clientValide("11");

        ClientDTO maj = service.updateCategorie(c.getId(), CategorieClient.VIP);

        assertEquals(CategorieClient.VIP, maj.getCategorieClient());
    }
}
