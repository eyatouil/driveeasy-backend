package com.driveeasy.location.client;

import com.driveeasy.location.client.dto.CategorieUpdatePayload;
import com.driveeasy.location.client.dto.ClientResponseDTO;
import com.driveeasy.location.client.dto.EligibiliteResponseDTO;

public interface ClientServiceClient {
    ClientResponseDTO getClientById(Long id);
    EligibiliteResponseDTO getEligibilite(Long id);
    ClientResponseDTO updateCategorie(Long id, CategorieUpdatePayload payload);
}
