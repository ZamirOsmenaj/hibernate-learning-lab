package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Company;

import java.time.LocalDate;

/** Table-per-concrete-class leaf: fully independent table "cars". */
public class Car extends Vehicle {

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
