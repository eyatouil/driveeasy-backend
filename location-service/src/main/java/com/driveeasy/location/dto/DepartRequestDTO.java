package com.driveeasy.location.dto;

public class DepartRequestDTO {
    private Integer kmDepart;

    public DepartRequestDTO() {}

    public DepartRequestDTO(Integer kmDepart) {
        this.kmDepart = kmDepart;
    }

    public Integer getKmDepart() { return kmDepart; }
    public void setKmDepart(Integer kmDepart) { this.kmDepart = kmDepart; }
}
