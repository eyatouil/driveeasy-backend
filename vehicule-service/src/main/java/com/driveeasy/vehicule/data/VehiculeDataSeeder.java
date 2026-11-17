package com.driveeasy.vehicule.data;

import com.driveeasy.vehicule.model.BoiteVitesse;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.StatutVehicule;
import com.driveeasy.vehicule.model.Vehicule;
import com.driveeasy.vehicule.repository.VehiculeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class VehiculeDataSeeder implements CommandLineRunner {

    private final VehiculeRepository repository;

    public VehiculeDataSeeder(VehiculeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        repository.save(new Vehicule(1L, "240 TUN 1021", "Peugeot", "208 GT", 2024, "Jaune Faro",
                Carburant.ESSENCE, BoiteVitesse.MANUELLE, 5, 14500, new BigDecimal("45.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&w=800&q=80"));

        repository.save(new Vehicule(2L, "238 TUN 4412", "Renault", "Clio 5 E-Tech", 2023, "Bleu Iron",
                Carburant.HYBRIDE, BoiteVitesse.AUTOMATIQUE, 5, 27800, new BigDecimal("50.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80"));

        repository.save(new Vehicule(3L, "235 TUN 7890", "Volkswagen", "Golf 8 Life", 2023, "Gris Urano",
                Carburant.DIESEL, BoiteVitesse.AUTOMATIQUE, 5, 41200, new BigDecimal("65.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80"));

        repository.save(new Vehicule(4L, "242 TUN 9901", "Tesla", "Model 3 Long Range", 2024, "Blanc Nacré",
                Carburant.ELECTRIQUE, BoiteVitesse.AUTOMATIQUE, 5, 11500, new BigDecimal("95.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=800&q=80"));

        repository.save(new Vehicule(5L, "231 TUN 3310", "Hyundai", "Tucson N-Line", 2023, "Gris Shadow",
                Carburant.HYBRIDE, BoiteVitesse.AUTOMATIQUE, 5, 34200, new BigDecimal("80.00"),
                StatutVehicule.LOUE, "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80"));

        repository.save(new Vehicule(6L, "241 TUN 6643", "Mercedes-Benz", "Classe A 200d", 2024, "Noir Cosmos",
                Carburant.DIESEL, BoiteVitesse.AUTOMATIQUE, 5, 18900, new BigDecimal("90.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80"));

        repository.save(new Vehicule(7L, "237 TUN 5124", "Toyota", "Yaris Cross", 2023, "Or Pur",
                Carburant.HYBRIDE, BoiteVitesse.AUTOMATIQUE, 5, 23400, new BigDecimal("55.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1590362891991-f776e747a588?auto=format&fit=crop&w=800&q=80"));

        repository.save(new Vehicule(8L, "229 TUN 1982", "Dacia", "Sandero Stepway", 2022, "Orange Atacama",
                Carburant.ESSENCE, BoiteVitesse.MANUELLE, 5, 47800, new BigDecimal("35.00"),
                StatutVehicule.MAINTENANCE, "https://images.unsplash.com/photo-1502877338535-766e1452684a?auto=format&fit=crop&w=800&q=80"));

        repository.save(new Vehicule(9L, "243 TUN 8109", "BMW", "Série 3 Touring M-Sport", 2024, "Bleu Portimao",
                Carburant.DIESEL, BoiteVitesse.AUTOMATIQUE, 5, 16200, new BigDecimal("110.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80"));

        repository.save(new Vehicule(10L, "236 TUN 2056", "Peugeot", "3008 Allure Pack", 2023, "Bleu Célèbes",
                Carburant.DIESEL, BoiteVitesse.AUTOMATIQUE, 5, 29400, new BigDecimal("75.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80"));
    }
}
