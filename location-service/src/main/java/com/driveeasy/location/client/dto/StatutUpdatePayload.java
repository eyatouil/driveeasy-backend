package com.driveeasy.location.client.dto;

public class StatutUpdatePayload {
    private String statut;
    private Integer kilometrage;

    public StatutUpdatePayload() {}

    public StatutUpdatePayload(String statut) {
        this.statut = statut;
    }

    public StatutUpdatePayload(String statut, Integer kilometrage) {
        this.statut = statut;
        this.kilometrage = kilometrage;
    }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getKilometrage() { return kilometrage; }
    public void setKilometrage(Integer kilometrage) { this.kilometrage = kilometrage; }
}
