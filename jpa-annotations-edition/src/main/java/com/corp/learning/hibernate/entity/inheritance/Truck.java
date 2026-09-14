package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Company;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Table-per-concrete-class leaf: fully independent table "trucks". */
@Entity
@Table(name = "trucks")
public class Truck extends Vehicle {

    @Column(name = "load_capacity_kg")
    private BigDecimal loadCapacityKg;

    public Truck() {
    }

    public Truck(String plateNumber, LocalDate purchaseDate, Company company, BigDecimal loadCapacityKg) {
        super(plateNumber, purchaseDate, company);
        this.loadCapacityKg = loadCapacityKg;
    }

    public BigDecimal getLoadCapacityKg() {
        return loadCapacityKg;
    }

    public void setLoadCapacityKg(BigDecimal loadCapacityKg) {
        this.loadCapacityKg = loadCapacityKg;
    }
}
