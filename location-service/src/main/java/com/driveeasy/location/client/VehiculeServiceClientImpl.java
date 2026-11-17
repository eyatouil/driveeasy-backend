package com.driveeasy.location.client;

import com.driveeasy.location.client.dto.StatutUpdatePayload;
import com.driveeasy.location.client.dto.VehiculeResponseDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class VehiculeServiceClientImpl implements VehiculeServiceClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://vehicule-service/vehicules";

    public VehiculeServiceClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public VehiculeResponseDTO getVehiculeById(Long id) {
        return restTemplate.getForObject(BASE_URL + "/" + id, VehiculeResponseDTO.class);
    }

    @Override
    public List<VehiculeResponseDTO> getVehiculesDisponibles() {
        ResponseEntity<List<VehiculeResponseDTO>> response = restTemplate.exchange(
                BASE_URL + "/disponibles",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VehiculeResponseDTO>>() {}
        );
        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }

    @Override
    public VehiculeResponseDTO updateStatut(Long id, StatutUpdatePayload payload) {
        HttpEntity<StatutUpdatePayload> requestEntity = new HttpEntity<>(payload);
        ResponseEntity<VehiculeResponseDTO> response = restTemplate.exchange(
                BASE_URL + "/" + id + "/statut",
                HttpMethod.PATCH,
                requestEntity,
                VehiculeResponseDTO.class
        );
        return response.getBody();
    }
}
