package com.driveeasy.location.dto;

public class TopVehiculeDTO {
    private Long vehiculeId;
    private String immatriculation;
    private String marqueModele;
    private long nombreLocations;

    public TopVehiculeDTO() {}

    public TopVehiculeDTO(Long vehiculeId, String immatriculation, String marqueModele, long nombreLocations) {
        this.vehiculeId = vehiculeId;
        this.immatriculation = immatriculation;
        this.marqueModele = marqueModele;
        this.nombreLocations = nombreLocations;
    }

    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }

    public String getMarqueModele() { return marqueModele; }
    public void setMarqueModele(String marqueModele) { this.marqueModele = marqueModele; }

    public long getNombreLocations() { return nombreLocations; }
    public void setNombreLocations(long nombreLocations) { this.nombreLocations = nombreLocations; }
}
