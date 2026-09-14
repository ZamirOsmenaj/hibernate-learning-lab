package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Company;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import java.time.LocalDate;

/**
 * Base of the TABLE PER CONCRETE CLASS hierarchy. Concrete subclasses:
 * {@link Car}, {@link Truck}.
 *
 * @Inheritance(TABLE_PER_CLASS) is the annotation equivalent of hbm's
 * &lt;union-subclass&gt;. There is NO "vehicles" table at all. Each
 * concrete subclass gets its own complete, independent table containing
 * BOTH the inherited columns and its own columns:
 *   cars   (id, plate_number, purchase_date, company_id, number_of_seats)
 *   trucks (id, plate_number, purchase_date, company_id, load_capacity_kg)
 *
 * Polymorphic queries ("from Vehicle") are implemented with a SQL UNION
 * across every subclass table - hence the name.
 *
 * Vehicle is declared `abstract` here (unlike the hbm.xml edition's plain
 * class + a separate abstract="true" mapping attribute): under JPA's
 * TABLE_PER_CLASS strategy, an abstract @Entity class produces NO table of
 * its own - only concrete, non-abstract subclasses get tables. This is the
 * annotation-idiomatic way to express the same "Vehicle itself is never
 * instantiated/stored directly" rule the hbm mapping stated explicitly.
 *
 * Id generation: plain IDENTITY per table would let two different vehicles
 * (a Car and a Truck) end up with the SAME numeric id - fine for direct
 * lookups by (id, type), but ambiguous for a bare polymorphic "get Vehicle
 * by id". A single shared sequence (used below, allocationSize = 1 to match
 * the hbm edition's un-batched "call nextval every time" behavior exactly)
 * avoids that by handing out globally unique ids across every subclass table.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicle_seq")
    @SequenceGenerator(name = "vehicle_seq", sequenceName = "vehicle_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
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
