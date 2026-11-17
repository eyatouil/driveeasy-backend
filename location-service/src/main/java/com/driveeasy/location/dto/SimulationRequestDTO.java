package com.driveeasy.location.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class SimulationRequestDTO {

    private Long clientId;
    private String categorieClient;

    @NotNull(message = "L'identifiant du véhicule est obligatoire")
    private Long vehiculeId;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin prévue est obligatoire")
    private LocalDate dateFinPrevue;

    public SimulationRequestDTO() {}

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getCategorieClient() { return categorieClient; }
    public void setCategorieClient(String categorieClient) { this.categorieClient = categorieClient; }

    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }
}
