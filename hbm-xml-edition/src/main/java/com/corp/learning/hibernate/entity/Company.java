package com.corp.learning.hibernate.entity;

import com.corp.learning.hibernate.entity.component.Address;
import com.corp.learning.hibernate.entity.inheritance.Vehicle;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * ROOT ENTITY of the whole model.
 *
 * On purpose, the Company -> Department association does NOT cascade delete
 * (see Company.hbm.xml, cascade="save-update" only, and Department.company_id
 * is NOT NULL with no ON DELETE CASCADE). That means:
 *
 *      If a Company still has Departments, deleting it will fail with a
 *      database foreign-key ConstraintViolationException.
 *
 * This is a deliberate, common real-world rule: "you can't delete the parent
 * while children still reference it" - see Demo01 for it firing live.
 */
public class Company {

    private Long id;
    private String name;
    private String registrationCode;
    private Address address;
    private LocalDate foundedDate;

    /** Optimistic locking column - see Demo04_OptimisticLocking */
    private int version;

    private Set<Department> departments = new HashSet<>();
    private Set<Vehicle> fleet = new HashSet<>();

    public Company() {
    }

    public Company(String name, String registrationCode, Address address, LocalDate foundedDate) {
        this.name = name;
        this.registrationCode = registrationCode;
        this.address = address;
        this.foundedDate = foundedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public LocalDate getFoundedDate() {
        return foundedDate;
    }

    public void setFoundedDate(LocalDate foundedDate) {
        this.foundedDate = foundedDate;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }

    public Set<Vehicle> getFleet() {
        return fleet;
    }

    public void setFleet(Set<Vehicle> fleet) {
        this.fleet = fleet;
    }

    /** Convenience method that keeps both sides of the bidirectional link in sync. */
    public void addDepartment(Department department) {
        departments.add(department);
        department.setCompany(this);
    }

    @Override
    public String toString() {
        return "Company{id=" + id + ", name='" + name + "', registrationCode='" + registrationCode + "'}";
    }
}
