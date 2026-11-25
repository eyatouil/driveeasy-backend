package com.driveeasy.vehicule.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

/** Appel vers location-service : véhicules occupés (RESERVEE / EN_COURS) sur une période. */
@Component
public class LocationServiceClient {

    private static final String BASE_URL = "http://location-service/locations";
    private final RestTemplate restTemplate;

    public LocationServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Long> getVehiculesOccupes(LocalDate dateDebut, LocalDate dateFin) {
        List<Long> ids = restTemplate.exchange(
                BASE_URL + "/vehicules-occupes?dateDebut=" + dateDebut + "&dateFin=" + dateFin,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Long>>() {}).getBody();
        return ids != null ? ids : List.of();
    }
}
