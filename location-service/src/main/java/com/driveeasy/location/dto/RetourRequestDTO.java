package com.driveeasy.location.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class RetourRequestDTO {

    @NotNull(message = "Le kilométrage au retour est obligatoire")
    private Integer kmRetour;

    private LocalDate dateRetourEffectif;

    public RetourRequestDTO() {}

    public RetourRequestDTO(Integer kmRetour, LocalDate dateRetourEffectif) {
        this.kmRetour = kmRetour;
        this.dateRetourEffectif = dateRetourEffectif;
    }

    public Integer getKmRetour() { return kmRetour; }
    public void setKmRetour(Integer kmRetour) { this.kmRetour = kmRetour; }

    public LocalDate getDateRetourEffectif() { return dateRetourEffectif; }
    public void setDateRetourEffectif(LocalDate dateRetourEffectif) { this.dateRetourEffectif = dateRetourEffectif; }
}
