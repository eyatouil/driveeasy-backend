package com.driveeasy.vehicule.dto;

import com.driveeasy.vehicule.model.BoiteVitesse;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.StatutVehicule;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class VehiculeUpdateDTO {

    private String marque;
    private String modele;

    @Min(value = 1990, message = "L'année doit être supérieure ou égale à 1990")
    private Integer annee;

    private String couleur;
    private Carburant carburant;
    private BoiteVitesse boite;

    @Min(value = 1, message = "Le véhicule doit avoir au moins 1 place")
    private Integer nbPlaces;

    @Min(value = 0, message = "Le kilométrage doit être positif ou nul")
    private Integer kilometrage;

    @DecimalMin(value = "0.01", inclusive = true, message = "Le prix journalier doit être strictement positif")
    private BigDecimal prixJournalier;

    private StatutVehicule statut;
    private String imageUrl;

    public VehiculeUpdateDTO() {}

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    public Carburant getCarburant() { return carburant; }
    public void setCarburant(Carburant carburant) { this.carburant = carburant; }

    public BoiteVitesse getBoite() { return boite; }
    public void setBoite(BoiteVitesse boite) { this.boite = boite; }

    public Integer getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(Integer nbPlaces) { this.nbPlaces = nbPlaces; }

    public Integer getKilometrage() { return kilometrage; }
    public void setKilometrage(Integer kilometrage) { this.kilometrage = kilometrage; }

    public BigDecimal getPrixJournalier() { return prixJournalier; }
    public void setPrixJournalier(BigDecimal prixJournalier) { this.prixJournalier = prixJournalier; }

    public StatutVehicule getStatut() { return statut; }
    public void setStatut(StatutVehicule statut) { this.statut = statut; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
