package com.driveeasy.vehicule.dto;

import com.driveeasy.vehicule.model.StatutVehicule;
import jakarta.validation.constraints.NotNull;

public class StatutUpdateDTO {

    @NotNull(message = "Le statut est obligatoire")
    private StatutVehicule statut;

    private Integer kilometrage;

    public StatutUpdateDTO() {}

    public StatutUpdateDTO(StatutVehicule statut) {
        this.statut = statut;
    }

    public StatutVehicule getStatut() { return statut; }
    public void setStatut(StatutVehicule statut) { this.statut = statut; }

    public Integer getKilometrage() { return kilometrage; }
    public void setKilometrage(Integer kilometrage) { this.kilometrage = kilometrage; }
}
