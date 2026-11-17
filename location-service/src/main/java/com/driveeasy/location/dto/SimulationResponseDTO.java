package com.driveeasy.location.dto;

import java.math.BigDecimal;

public class SimulationResponseDTO {
    private Long vehiculeId;
    private String vehiculeMarqueModele;
    private BigDecimal prixJournalier;
    private long nombreJours;
    private BigDecimal montantBrut;
    private String categorieClient;
    private BigDecimal tauxRemise;
    private BigDecimal montantRemise;
    private BigDecimal montantTotalEstime;

    public SimulationResponseDTO() {}

    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }

    public String getVehiculeMarqueModele() { return vehiculeMarqueModele; }
    public void setVehiculeMarqueModele(String vehiculeMarqueModele) { this.vehiculeMarqueModele = vehiculeMarqueModele; }

    public BigDecimal getPrixJournalier() { return prixJournalier; }
    public void setPrixJournalier(BigDecimal prixJournalier) { this.prixJournalier = prixJournalier; }

    public long getNombreJours() { return nombreJours; }
    public void setNombreJours(long nombreJours) { this.nombreJours = nombreJours; }

    public BigDecimal getMontantBrut() { return montantBrut; }
    public void setMontantBrut(BigDecimal montantBrut) { this.montantBrut = montantBrut; }

    public String getCategorieClient() { return categorieClient; }
    public void setCategorieClient(String categorieClient) { this.categorieClient = categorieClient; }

    public BigDecimal getTauxRemise() { return tauxRemise; }
    public void setTauxRemise(BigDecimal tauxRemise) { this.tauxRemise = tauxRemise; }

    public BigDecimal getMontantRemise() { return montantRemise; }
    public void setMontantRemise(BigDecimal montantRemise) { this.montantRemise = montantRemise; }

    public BigDecimal getMontantTotalEstime() { return montantTotalEstime; }
    public void setMontantTotalEstime(BigDecimal montantTotalEstime) { this.montantTotalEstime = montantTotalEstime; }
}
