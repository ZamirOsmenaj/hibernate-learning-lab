package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Company;

import java.time.LocalDate;

/**
 * Base of the TABLE PER CONCRETE CLASS hierarchy (<union-subclass>).
 * Concrete subclasses: {@link Car}, {@link Truck}.
 *
 * There is NO "vehicles" table at all. Each concrete subclass gets its own
 * complete, independent table containing BOTH the inherited columns and its
 * own columns:
 *   cars   (id, plate_number, purchase_date, company_id, number_of_seats)
 *   trucks (id, plate_number, purchase_date, company_id, load_capacity_kg)
 *
 * Polymorphic queries ("from Vehicle") are implemented with a SQL UNION
 * across every subclass table - hence the name.
 *
 * Pros: no join needed for concrete-type access; each subclass row is
 *       self-contained.
 * Cons: id generation can't use a simple per-table IDENTITY/SERIAL sequence
 *       shared cleanly (a hi/lo or a shared sequence is typically used, as
 *       done here); polymorphic queries are the most expensive of the three
 *       strategies.
 */
public class Vehicle {

    private Long id;
    private String plateNumber;
    private LocalDate purchaseDate;
    private Company company;

    public Vehicle() {
    }

    public Vehicle(String plateNumber, LocalDate purchaseDate, Company company) {
        this.plateNumber = plateNumber;
        this.purchaseDate = purchaseDate;
        this.company = company;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", plateNumber='" + plateNumber + "'}";
    }
}
