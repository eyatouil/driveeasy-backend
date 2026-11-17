package com.driveeasy.client.dto;

import com.driveeasy.client.model.CategorieClient;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public class ClientUpdateDTO {

    private String nom;
    private String prenom;
    private LocalDate dateNaissance;

    @Email(message = "L'adresse email n'est pas valide")
    private String email;

    private String telephone;
    private String adresse;
    private String numeroPermis;
    private LocalDate dateExpirationPermis;
    private CategorieClient categorieClient;

    public ClientUpdateDTO() {}

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getNumeroPermis() { return numeroPermis; }
    public void setNumeroPermis(String numeroPermis) { this.numeroPermis = numeroPermis; }

    public LocalDate getDateExpirationPermis() { return dateExpirationPermis; }
    public void setDateExpirationPermis(LocalDate dateExpirationPermis) { this.dateExpirationPermis = dateExpirationPermis; }

    public CategorieClient getCategorieClient() { return categorieClient; }
    public void setCategorieClient(CategorieClient categorieClient) { this.categorieClient = categorieClient; }
}
