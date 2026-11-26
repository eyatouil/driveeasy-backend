package com.driveeasy.location.data;

import com.driveeasy.location.model.Location;
import com.driveeasy.location.model.StatutLocation;
import com.driveeasy.location.repository.LocationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class LocationDataSeeder implements CommandLineRunner {

    private final LocationRepository repository;

    public LocationDataSeeder(LocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        LocalDate today = LocalDate.now();

        // 1. Location EN COURS (Hyundai Tucson loué par Youssef Trabelsi)
        repository.save(new Location(
                1L, 2L, 5L,
                today.minusDays(2), today.plusDays(3),
                null, 34200, null,
                new BigDecimal("80.00"), new BigDecimal("380.00"), BigDecimal.ZERO,
                StatutLocation.EN_COURS,
                LocalDateTime.now().minusDays(2)
        ));

        // 2. Location RESERVEE (Peugeot 208 réservée par Nour Ben Salem)
        repository.save(new Location(
                2L, 3L, 1L,
                today.plusDays(1), today.plusDays(4),
                null, 14500, null,
                new BigDecimal("45.00"), new BigDecimal("135.00"), BigDecimal.ZERO,
                StatutLocation.RESERVEE,
                LocalDateTime.now().minusHours(4)
        ));

        // 3. Location TERMINEE (Tesla Model 3 louée par Karim Ben Romdhane - VIP 10% remise)
        repository.save(new Location(
                3L, 1L, 4L,
                today.minusDays(10), today.minusDays(7),
                today.minusDays(7), 10800, 11500,
                new BigDecimal("95.00"), new BigDecimal("256.50"), BigDecimal.ZERO,
                StatutLocation.TERMINEE,
                LocalDateTime.now().minusDays(10)
        ));

        // 4. Location TERMINEE AVEC RETARD ET PÉNALITÉ (Renault Clio 5 louée par Mohamed Ali Ayari)
        // 3 jours prévus (150€) + 2 jours de retard = pénalité 2 * 1.5 * 50 = 150€ -> Total 300€
        repository.save(new Location(
                4L, 6L, 2L,
                today.minusDays(8), today.minusDays(5),
                today.minusDays(3), 26500, 27800,
                new BigDecimal("50.00"), new BigDecimal("300.00"), new BigDecimal("150.00"),
                StatutLocation.TERMINEE,
                LocalDateTime.now().minusDays(8)
        ));

        // 5. Location TERMINEE (Mercedes Classe A louée par Karim Ben Romdhane)
        repository.save(new Location(
                5L, 1L, 6L,
                today.minusDays(20), today.minusDays(15),
                today.minusDays(15), 16200, 17500,
                new BigDecimal("90.00"), new BigDecimal("405.00"), BigDecimal.ZERO,
                StatutLocation.TERMINEE,
                LocalDateTime.now().minusDays(20)
        ));

        // 6. Location ANNULEE
        repository.save(new Location(
                6L, 6L, 7L,
                today.minusDays(4), today.minusDays(1),
                null, 23400, null,
                new BigDecimal("55.00"), new BigDecimal("165.00"), BigDecimal.ZERO,
                StatutLocation.ANNULEE,
                LocalDateTime.now().minusDays(5)
        ));
    }
}
