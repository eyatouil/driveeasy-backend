package com.driveeasy.location.client;

import com.driveeasy.location.client.dto.CategorieUpdatePayload;
import com.driveeasy.location.client.dto.ClientResponseDTO;
import com.driveeasy.location.client.dto.EligibiliteResponseDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ClientServiceClientImpl implements ClientServiceClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://client-service/clients";

    public ClientServiceClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ClientResponseDTO getClientById(Long id) {
        return restTemplate.getForObject(BASE_URL + "/" + id, ClientResponseDTO.class);
    }

    @Override
    public EligibiliteResponseDTO getEligibilite(Long id) {
        return restTemplate.getForObject(BASE_URL + "/" + id + "/eligibilite", EligibiliteResponseDTO.class);
    }

    @Override
    public ClientResponseDTO updateCategorie(Long id, CategorieUpdatePayload payload) {
        HttpEntity<CategorieUpdatePayload> requestEntity = new HttpEntity<>(payload);
        ResponseEntity<ClientResponseDTO> response = restTemplate.exchange(
                BASE_URL + "/" + id + "/categorie",
                HttpMethod.PATCH,
                requestEntity,
                ClientResponseDTO.class
        );
        return response.getBody();
    }
}
