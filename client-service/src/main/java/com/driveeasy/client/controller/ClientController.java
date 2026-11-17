package com.driveeasy.client.controller;

import com.driveeasy.client.dto.*;
import com.driveeasy.client.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<ClientDTO>> searchClients(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(clientService.searchClients(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@Valid @RequestBody ClientCreateDTO createDTO) {
        ClientDTO created = clientService.createClient(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateDTO updateDTO) {
        return ResponseEntity.ok(clientService.updateClient(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/eligibilite")
    public ResponseEntity<EligibiliteDTO> checkEligibilite(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.checkEligibilite(id));
    }

    @PatchMapping("/{id}/categorie")
    public ResponseEntity<ClientDTO> updateCategorie(
            @PathVariable Long id,
            @Valid @RequestBody CategorieUpdateDTO categorieDTO) {
        return ResponseEntity.ok(clientService.updateCategorie(id, categorieDTO.getCategorie()));
    }
}
