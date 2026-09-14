package com.corp.learning.hibernate.entity.component;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * VALUE TYPE (not an entity) mapped with JPA's {@code @Embeddable}.
 *
 * This is the annotation-based equivalent of a Hibernate {@code <component>}:
 * an Address has no identity or table of its own - its columns are folded
 * directly into the owning entity's table (Company / Employee in this
 * project, both via {@code @Embedded}). It has no lifecycle: you never
 * save/delete an Address by itself, it lives and dies with its owner.
 *
 * Rules JPA enforces for embeddables (same as Hibernate's <component>):
 *  - must have a no-arg constructor (the provider instantiates it via reflection)
 *  - equals()/hashCode() should be based on its fields (value semantics)
 *
 * Column names here (street, city, postalCode, country) do NOT automatically
 * become "address_street" etc. in the owner's table - each owner supplies
 * that mapping explicitly via @AttributeOverrides on its @Embedded field.
 * See Company.java and Employee.java.
 */
@Embeddable
public class Address implements Serializable {

    // No @Column here: the actual column names are supplied per-owner via
    // @AttributeOverride, since this same Address is embedded into two
    // different tables (companies, employees) that both happen to use the
    // same "address_*" column naming convention - but JPA never assumes
    // that, it always requires the owner to say so explicitly.
    private String street;
    private String city;
    private String postalCode;
    private String country;

    public Address() {
        // required by JPA
    }

    public Address(String street, String city, String postalCode, String country) {
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return street + ", " + postalCode + " " + city + ", " + country;
    }
}
