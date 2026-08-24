package com.corp.learning.hibernate.entity.component;

import java.io.Serializable;

/**
 * VALUE TYPE (not an entity) mapped with Hibernate's <component> element.
 *
 * A component has no identity or table of its own - its columns are folded
 * directly into the owning entity's table (Company / Employee in this project).
 * It has no lifecycle: you never save/delete an Address by itself, it lives
 * and dies with its owner.
 *
 * Rules enforced by Hibernate for components:
 *  - must have a no-arg constructor (Hibernate instantiates it via reflection)
 *  - equals()/hashCode() should be based on its fields (value semantics)
 */
public class Address implements Serializable {

    private String street;
    private String city;
    private String postalCode;
    private String country;

    public Address() {
        // required by Hibernate
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
