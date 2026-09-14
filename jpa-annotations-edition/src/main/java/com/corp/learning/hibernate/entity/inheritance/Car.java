package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Company;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

/** Table-per-concrete-class leaf: fully independent table "cars". */
@Entity
@Table(name = "cars")
public class Car extends Vehicle {

    @Column(name = "number_of_seats")
    private Integer numberOfSeats;

    public Car() {
    }

    public Car(String plateNumber, LocalDate purchaseDate, Company company, Integer numberOfSeats) {
        super(plateNumber, purchaseDate, company);
        this.numberOfSeats = numberOfSeats;
    }

    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }
}
