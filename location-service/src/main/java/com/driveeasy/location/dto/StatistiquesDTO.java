package com.driveeasy.location.dto;

import java.math.BigDecimal;
import java.util.List;

public class StatistiquesDTO {
    private BigDecimal chiffreAffairesTotal;
    private BigDecimal chiffreAffairesMoisCourant;
    private double tauxOccupation;
    private long totalLocations;
    private long locationsEnCours;
    private long locationsTerminees;
    private long locationsReservees;
    private long locationsAnnulees;
    private List<TopVehiculeDTO> topVehicules;

    public StatistiquesDTO() {}

    public BigDecimal getChiffreAffairesTotal() { return chiffreAffairesTotal; }
    public void setChiffreAffairesTotal(BigDecimal chiffreAffairesTotal) { this.chiffreAffairesTotal = chiffreAffairesTotal; }

    public BigDecimal getChiffreAffairesMoisCourant() { return chiffreAffairesMoisCourant; }
    public void setChiffreAffairesMoisCourant(BigDecimal chiffreAffairesMoisCourant) { this.chiffreAffairesMoisCourant = chiffreAffairesMoisCourant; }

    public double getTauxOccupation() { return tauxOccupation; }
    public void setTauxOccupation(double tauxOccupation) { this.tauxOccupation = tauxOccupation; }

    public long getTotalLocations() { return totalLocations; }
    public void setTotalLocations(long totalLocations) { this.totalLocations = totalLocations; }

    public long getLocationsEnCours() { return locationsEnCours; }
    public void setLocationsEnCours(long locationsEnCours) { this.locationsEnCours = locationsEnCours; }

    public long getLocationsTerminees() { return locationsTerminees; }
    public void setLocationsTerminees(long locationsTerminees) { this.locationsTerminees = locationsTerminees; }

    public long getLocationsReservees() { return locationsReservees; }
    public void setLocationsReservees(long locationsReservees) { this.locationsReservees = locationsReservees; }

    public long getLocationsAnnulees() { return locationsAnnulees; }
    public void setLocationsAnnulees(long locationsAnnulees) { this.locationsAnnulees = locationsAnnulees; }

    public List<TopVehiculeDTO> getTopVehicules() { return topVehicules; }
    public void setTopVehicules(List<TopVehiculeDTO> topVehicules) { this.topVehicules = topVehicules; }
}
