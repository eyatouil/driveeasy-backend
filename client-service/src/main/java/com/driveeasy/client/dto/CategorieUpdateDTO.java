package com.driveeasy.client.dto;

import com.driveeasy.client.model.CategorieClient;
import jakarta.validation.constraints.NotNull;

public class CategorieUpdateDTO {

    @NotNull(message = "La catégorie est obligatoire")
    private CategorieClient categorie;

    public CategorieUpdateDTO() {}

    public CategorieUpdateDTO(CategorieClient categorie) {
        this.categorie = categorie;
    }

    public CategorieClient getCategorie() { return categorie; }
    public void setCategorie(CategorieClient categorie) { this.categorie = categorie; }
}
