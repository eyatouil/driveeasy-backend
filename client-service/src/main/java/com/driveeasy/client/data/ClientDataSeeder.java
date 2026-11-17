package com.driveeasy.client.data;

import com.driveeasy.client.model.CategorieClient;
import com.driveeasy.client.model.Client;
import com.driveeasy.client.repository.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ClientDataSeeder implements CommandLineRunner {

    private final ClientRepository repository;

    public ClientDataSeeder(ClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        // 1. Client VIP éligible
        repository.save(new Client(1L, "08123456", "Ben Romdhane", "Karim",
                LocalDate.of(1989, 4, 15), "karim.benromdhane@driveeasy.tn", "+216 98 123 456",
                "Avenue Habib Bourguiba, Tunis", "TN-9812345",
                LocalDate.now().plusYears(5), CategorieClient.VIP,
                LocalDate.of(2022, 1, 10)));

        // 2. Client FIDÈLE éligible
        repository.save(new Client(2L, "09234567", "Trabelsi", "Youssef",
                LocalDate.of(1996, 8, 22), "youssef.trabelsi@gmail.com", "+216 22 345 678",
                "Les Berges du Lac 2, Tunis", "TN-0923456",
                LocalDate.now().plusYears(3), CategorieClient.FIDELE,
                LocalDate.of(2023, 3, 14)));

        // 3. Client STANDARD éligible
        repository.save(new Client(3L, "11345678", "Ben Salem", "Nour",
                LocalDate.of(2000, 11, 5), "nour.bensalem@yahoo.fr", "+216 55 789 012",
                "Ennasr 2, Ariana", "TN-1134567",
                LocalDate.now().plusYears(4), CategorieClient.STANDARD,
                LocalDate.of(2024, 6, 1)));

        // 4. Client Trop Jeune (< 21 ans) - Test Inéligibilité Âge
        repository.save(new Client(4L, "14567890", "Jaziri", "Amine",
                LocalDate.now().minusYears(19), "amine.jaziri@outlook.com", "+216 29 456 123",
                "Menzah 9, Tunis", "TN-1456789",
                LocalDate.now().plusYears(2), CategorieClient.STANDARD,
                LocalDate.of(2025, 2, 10)));

        // 5. Client Permis Expiré - Test Inéligibilité Permis
        repository.save(new Client(5L, "07654321", "Mansouri", "Sarah",
                LocalDate.of(1993, 2, 18), "sarah.mansouri@gmail.com", "+216 97 654 321",
                "Carthage Dermech, Tunis", "TN-0765432",
                LocalDate.now().minusMonths(3), CategorieClient.STANDARD,
                LocalDate.of(2023, 9, 20)));

        // 6. Client STANDARD régulier
        repository.save(new Client(6L, "10456789", "Ayari", "Mohamed Ali",
                LocalDate.of(1992, 7, 30), "m.ali.ayari@gmail.com", "+216 21 098 765",
                "La Marsa, Tunis", "TN-1045678",
                LocalDate.now().plusYears(6), CategorieClient.STANDARD,
                LocalDate.of(2024, 1, 15)));
    }
}
