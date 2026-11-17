package com.driveeasy.location.dto;

import com.driveeasy.location.client.dto.ClientResponseDTO;
import com.driveeasy.location.client.dto.VehiculeResponseDTO;
import com.driveeasy.location.model.StatutLocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LocationEnrichieDTO {
    private Long id;
    private Long clientId;
    private Long vehiculeId;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateRetourEffectif;
    private int kmDepart;
    private Integer kmRetour;
    private BigDecimal prixJournalierApplique;
    private BigDecimal montantTotal;
    private BigDecimal penalite;
    private StatutLocation statut;
    private LocalDateTime dateCreation;
    private long dureeJours;
    private BigDecimal remisePourcentage;
    private BigDecimal remiseMontant;

    private ClientResponseDTO client;
    private VehiculeResponseDTO vehicule;

    public LocationEnrichieDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }

    public LocalDate getDateRetourEffectif() { return dateRetourEffectif; }
    public void setDateRetourEffectif(LocalDate dateRetourEffectif) { this.dateRetourEffectif = dateRetourEffectif; }

    public int getKmDepart() { return kmDepart; }
    public void setKmDepart(int kmDepart) { this.kmDepart = kmDepart; }

    public Integer getKmRetour() { return kmRetour; }
    public void setKmRetour(Integer kmRetour) { this.kmRetour = kmRetour; }

    public BigDecimal getPrixJournalierApplique() { return prixJournalierApplique; }
    public void setPrixJournalierApplique(BigDecimal prixJournalierApplique) { this.prixJournalierApplique = prixJournalierApplique; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public BigDecimal getPenalite() { return penalite; }
    public void setPenalite(BigDecimal penalite) { this.penalite = penalite; }

    public StatutLocation getStatut() { return statut; }
    public void setStatut(StatutLocation statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public long getDureeJours() { return dureeJours; }
    public void setDureeJours(long dureeJours) { this.dureeJours = dureeJours; }

    public BigDecimal getRemisePourcentage() { return remisePourcentage; }
    public void setRemisePourcentage(BigDecimal remisePourcentage) { this.remisePourcentage = remisePourcentage; }

    public BigDecimal getRemiseMontant() { return remiseMontant; }
    public void setRemiseMontant(BigDecimal remiseMontant) { this.remiseMontant = remiseMontant; }

    public ClientResponseDTO getClient() { return client; }
    public void setClient(ClientResponseDTO client) { this.client = client; }

    public VehiculeResponseDTO getVehicule() { return vehicule; }
    public void setVehicule(VehiculeResponseDTO vehicule) { this.vehicule = vehicule; }
}
