# DriveEasy - Plateforme de Location de Véhicules (Backend Microservices)

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-blue.svg)](https://spring.io/projects/spring-cloud)
[![Zipkin](https://img.shields.io/badge/Zipkin-Distributed%20Tracing-yellow.svg)](https://zipkin.io/)

Prototype fonctionnel d'architecture microservices pour la société **DriveEasy**, spécialisée dans la location de véhicules de courte et moyenne durée.

---

## 1. Architecture du Système

L'application respecte les principes d'une architecture distribuée découplée et résiliente :

```
                        +----------------------------+
                        |   Angular 22 (Port 4200)   |
                        +--------------+-------------+
                                       |
                                       v
                        +----------------------------+
                        |  API Gateway (Port 8080)   |
                        +--------------+-------------+
                                       |
        +------------------------------+------------------------------+
        |                              |                              |
        v                              v                              v
+---------------+              +---------------+              +---------------+
| vehicule-svc  |              |  client-svc   |              | location-svc  |
|  (Port 8081)  |              |  (Port 8082)  |              |  (Port 8083)  |
+-------+-------+              +-------+-------+              +-------+-------+
        ^                              ^                              ^
        |                              |                              |
        +------------------------------+------------------------------+
                                       |
                +----------------------+----------------------+
                |                                             |
                v                                             v
  +---------------------------+                 +---------------------------+
  | Service Registry (Eureka) |                 |       Config Server       |
  |        (Port 8761)        |                 |        (Port 8888)        |
  +---------------------------+                 +---------------------------+
```

### Rôle et Ports des Composants

| Composant | Technologie | Port | Rôle |
| :--- | :--- | :---: | :--- |
| **Service Registry** | Spring Cloud Netflix Eureka | **8761** | Annuaire des services, découverte dynamique |
| **Config Server** | Spring Cloud Config Server | **8888** | Configuration centralisée externalisée |
| **vehicule-service** | Spring Boot 3.x / Web | **8081** | Gestion du parc, disponibilité, kilométrage |
| **client-service** | Spring Boot 3.x / Web | **8082** | Gestion des clients, contrôle d'éligibilité |
| **location-service** | Spring Boot 3.x / RestTemplate `@LoadBalanced` | **8083** | Cycle de vie des locations, facturation |
| **API Gateway** | Spring Cloud Gateway | **8080** | Point d'entrée unique, routage, CORS, filtres |
| **Zipkin** | Docker (`openzipkin/zipkin`) | **9411** | Traçage distribué des requêtes |

> **Règle stricte respectée** : Les données sont chargées uniquement en **RAM** via des structures concurrentes thread-safe (`ConcurrentHashMap`). Aucune base de données relationnelle ou NoSQL n'est utilisée.

---

## 2. Prérequis

- **JDK 21** installé (`java -version`)
- **Apache Maven 3.8+** (`mvn -version`)
- **Docker** & **Docker Compose** pour le serveur Zipkin
- **Git**

---

## 3. Instructions d'Installation et Lancement

### Étape 0 : Cloner les dépôts et compiler
La configuration est externalisée dans un **dépôt Git séparé** (`driveeasy-config`) lu par le Config Server.
```bash
git clone https://github.com/eyatouil/driveeasy-backend.git
git clone https://github.com/eyatouil/driveeasy-config.git   # à publier sur GitHub (contenu du dossier driveeasy-config)
cd driveeasy-backend
mvn clean install -DskipTests
```
Renseigner l'URI du dépôt de configuration avant de lancer le Config Server :
```bash
# Windows (cmd)      set CONFIG_GIT_URI=https://github.com/eyatouil/driveeasy-config.git
# Linux / macOS      export CONFIG_GIT_URI=https://github.com/eyatouil/driveeasy-config.git
```
(ou modifier `config-server/src/main/resources/application.yml`). Hors-ligne : `mvn spring-boot:run -Dspring-boot.run.profiles=native` lit le dossier voisin `../driveeasy-config`.

> Les microservices et la gateway **ne démarrent pas sans Config Server** (import non optionnel) : respecter l'ordre ci-dessous.
> Démarrage automatisé : `start-backend.bat` (Windows) ou `./start-backend.sh` (Linux/macOS).

### Étape 1 : Démarrer Zipkin (Monitoring / Traçage)
```bash
docker compose up -d
# Accès interface Zipkin : http://localhost:9411
```

### Étape 2 : Ordre de Démarrage des Microservices

Pour garantir la découverte et la distribution de configuration, lancez les microservices dans l'ordre suivant (dans des terminaux distincts) :

1. **Service Registry (Eureka)**
   ```bash
   cd service-registry
   mvn spring-boot:run
   # Vérification : http://localhost:8761
   ```

2. **Config Server**
   ```bash
   cd config-server
   mvn spring-boot:run
   # Vérification : http://localhost:8888/vehicule-service/default (doit renvoyer les propriétés du dépôt Git)
   ```

3. **Microservices Métier**
   ```bash
   # Terminal A - vehicule-service
   cd vehicule-service
   mvn spring-boot:run

   # Terminal B - client-service
   cd client-service
   mvn spring-boot:run

   # Terminal C - location-service
   cd location-service
   mvn spring-boot:run
   ```

4. **API Gateway**
   ```bash
   cd api-gateway
   mvn spring-boot:run
   # Vérification : http://localhost:8080/actuator/health
   ```

---

## 4. Spécifications des Endpoints via API Gateway (Port 8080)

Tout le trafic client passe par `http://localhost:8080/api/...` :

### 4.1 Véhicules (`/api/vehicules`)
- `GET /api/vehicules` : Liste des véhicules avec filtres (`query`, `carburant`, `statut`, `prixMax`)
- `GET /api/vehicules/disponibles` : Véhicules disponibles pour une période
- `GET /api/vehicules/{id}` : Détails d'un véhicule
- `POST /api/vehicules` : Ajout d'un véhicule (vérification unicité immatriculation -> 409)
- `PUT /api/vehicules/{id}` : Modification
- `DELETE /api/vehicules/{id}` : Suppression (refusée si loué -> 409)
- `PATCH /api/vehicules/{id}/statut` : Changement de statut / kilométrage

### 4.2 Clients (`/api/clients`)
- `GET /api/clients` : Recherche par nom / CIN / email
- `GET /api/clients/{id}` : Détails d'un client
- `GET /api/clients/{id}/eligibilite` : Vérification éligibilité (âge >= 21 ans et permis valide)
- `POST /api/clients` : Création client (CIN, email, numéro permis uniques)
- `PUT /api/clients/{id}` : Modification
- `DELETE /api/clients/{id}` : Suppression
- `PATCH /api/clients/{id}/categorie` : Mise à jour catégorie de fidélité (`STANDARD`, `FIDELE`, `VIP`)

### 4.3 Locations (`/api/locations`)
- `GET /api/locations` : Liste filtrable par statut, client, véhicule, période
- `GET /api/locations/{id}` : Détail enrichi (agrégation des données Client et Véhicule)
- `POST /api/locations` : Réservation avec validation inter-services
- `POST /api/locations/simulation` : Simulation tarifaire avec remise fidélité
- `PATCH /api/locations/{id}/depart` : Démarrage (véhicule passe à LOUE)
- `PATCH /api/locations/{id}/retour` : Clôture, calcul pénalités de retard (1.5x), mise à jour fidélité
- `PATCH /api/locations/{id}/annulation` : Annulation
- `GET /api/locations/statistiques` : CA, taux d'occupation, top véhicules

---

> **Note datasource (§4.2)** : aucune propriété `spring.datasource.*` n'est externalisée car les bases de données sont interdites ; les propriétés externalisées sont le port, Eureka, Zipkin, les propriétés métier (`driveeasy.*`) et, pour la gateway, les routes et le CORS.

> **Routage** : `GET /api/locations/statistiques` est correctement résolu car Spring MVC donne la priorité au chemin littéral sur `/{id}` ; `StripPrefix=1` retire `/api` avant l'appel du service.

---

## 5. Jeux de Données Initiaux en RAM

Au démarrage, les services sont pré-peuplés avec des jeux de données réalistes :

### Véhicules
- **ID 1** : Peugeot 208 GT (`240 TUN 1021`), 45.00€/j, DISPONIBLE
- **ID 2** : Renault Clio 5 E-Tech (`238 TUN 4412`), 50.00€/j, DISPONIBLE
- **ID 3** : Volkswagen Golf 8 (`235 TUN 7890`), 65.00€/j, DISPONIBLE
- **ID 4** : Tesla Model 3 Long Range (`242 TUN 9901`), 95.00€/j, DISPONIBLE
- **ID 5** : Hyundai Tucson N-Line (`231 TUN 3310`), 80.00€/j, LOUE
- **ID 6** : Mercedes-Benz Classe A (`241 TUN 6643`), 90.00€/j, DISPONIBLE
- **ID 8** : Dacia Sandero Stepway (`229 TUN 1982`), 35.00€/j, MAINTENANCE

### Clients
- **ID 1** : Karim Ben Romdhane (`08123456`, VIP - 10% remise)
- **ID 2** : Youssef Trabelsi (`09234567`, FIDELE - 5% remise)
- **ID 3** : Nour Ben Salem (`11345678`, STANDARD)
- **ID 4** : Amine Jaziri (19 ans -> Inéligible : âge < 21)
- **ID 5** : Sarah Mansouri (Permis expiré -> Inéligible)

---

## 6. Règles Métier Implémentées

1. **Éligibilité à la location** :
   $$\text{Éligible} \iff \text{Âge} \ge 21 \land \text{DateExpirationPermis} > \text{DateDuJour}$$
2. **Calcul du tarif prévisionnel** :
   $$\text{Prix Total} = \text{Jours} \times \text{PrixJournalier} \times (1 - \text{RemiseFidélité})$$
   - `STANDARD` : Remise 0%
   - `FIDELE` : Remise 5%
   - `VIP` : Remise 10%
3. **Pénalité de retard lors du retour** :
   $$\text{Pénalité} = \text{JoursRetard} \times 1.5 \times \text{PrixJournalier}$$
4. **Promotion automatique de fidélité** :
   - Après 5 locations terminées $\rightarrow$ `FIDELE`
   - Après 15 locations terminées $\rightarrow$ `VIP`

---

## 7. Traçage Distribué avec Zipkin

Toutes les requêtes traversant l'API Gateway et les microservices propagent le contexte de traçage B3/W3C via Micrometer Tracing.
Consultez l'interface Zipkin sur `http://localhost:9411` pour visualiser :
- Le graphe de dépendance inter-services
- Le temps de réponse de chaque composant
- Les traces transversales : `api-gateway` $\rightarrow$ `location-service` $\rightarrow$ `vehicule-service` & `client-service`

## Compléments (corrections de cohérence)
- `GET /locations?page=0&size=10` : pagination optionnelle (en-têtes `X-Total-Count`, `X-Total-Pages`).
- `DELETE /clients/{id}` : refusé (409) si le client a une location RESERVEE ou EN_COURS ; 503 si location-service est injoignable.
- Retour de véhicule : le véhicule est libéré avant la clôture ; en cas d'échec la location reste EN_COURS (503).
- Appels inter-services PATCH : client HTTP du JDK (HttpURLConnection ne supporte pas PATCH).
- Prix = jours × prix journalier × coefficient catégorie − remise fidélité (coefficients dans `driveeasy-config/location-service.yml`).
- Année d'un véhicule ≤ année courante.
