package com.driveeasy.client.service;

import com.driveeasy.client.dto.*;
import com.driveeasy.client.model.CategorieClient;

import java.util.List;

public interface ClientService {
    List<ClientDTO> searchClients(String query);
    ClientDTO getClientById(Long id);
    ClientDTO createClient(ClientCreateDTO createDTO);
    ClientDTO updateClient(Long id, ClientUpdateDTO updateDTO);
    void deleteClient(Long id);
    EligibiliteDTO checkEligibilite(Long id);
    ClientDTO updateCategorie(Long id, CategorieClient categorie);
}
