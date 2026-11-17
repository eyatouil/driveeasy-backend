package com.driveeasy.location.client.dto;

public class CategorieUpdatePayload {
    private String categorie;

    public CategorieUpdatePayload() {}

    public CategorieUpdatePayload(String categorie) {
        this.categorie = categorie;
    }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
}
