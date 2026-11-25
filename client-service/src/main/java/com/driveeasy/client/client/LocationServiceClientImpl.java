package com.driveeasy.client.client;

import com.driveeasy.client.exception.ServiceUnavailableException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class LocationServiceClientImpl implements LocationServiceClient {

    private static final String BASE_URL = "http://location-service/locations";
    private final RestTemplate restTemplate;

    public LocationServiceClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean hasActiveLocation(Long clientId) {
        try {
            for (String statut : new String[]{"RESERVEE", "EN_COURS"}) {
                List<Map<String, Object>> body = restTemplate.exchange(
                        BASE_URL + "?clientId=" + clientId + "&statut=" + statut,
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
                if (body != null && !body.isEmpty()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new ServiceUnavailableException(
                    "Impossible de vérifier les locations actives du client (location-service injoignable)", e);
        }
    }
}
