package com.driveeasy.location.client.dto;

import java.time.LocalDate;

public class EligibiliteResponseDTO {
    private Long clientId;
    private String nomComplet;
    private boolean eligible;
    private int age;
    private boolean ageValide;
    private boolean permisValide;
    private LocalDate dateExpirationPermis;
    private String motifRefus;
    private String categorieClient;

    public EligibiliteResponseDTO() {}

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public boolean isAgeValide() { return ageValide; }
    public void setAgeValide(boolean ageValide) { this.ageValide = ageValide; }

    public boolean isPermisValide() { return permisValide; }
    public void setPermisValide(boolean permisValide) { this.permisValide = permisValide; }

    public LocalDate getDateExpirationPermis() { return dateExpirationPermis; }
    public void setDateExpirationPermis(LocalDate dateExpirationPermis) { this.dateExpirationPermis = dateExpirationPermis; }

    public String getMotifRefus() { return motifRefus; }
    public void setMotifRefus(String motifRefus) { this.motifRefus = motifRefus; }

    public String getCategorieClient() { return categorieClient; }
    public void setCategorieClient(String categorieClient) { this.categorieClient = categorieClient; }
}
