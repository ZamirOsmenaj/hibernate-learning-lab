package com.corp.learning.hibernate.entity;

import com.corp.learning.hibernate.entity.component.Address;
import com.corp.learning.hibernate.entity.inheritance.Vehicle;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * ROOT ENTITY of the whole model.
 *
 * On purpose, the Company -> Department association does NOT cascade delete
 * (cascade = {PERSIST, MERGE} only below, and Department.company_id is NOT
 * NULL with no ON DELETE CASCADE). That means:
 *
 *      If a Company still has Departments, deleting it will fail with a
 *      database foreign-key ConstraintViolationException.
 *
 * This is a deliberate, common real-world rule: "you can't delete the parent
 * while children still reference it" - see Demo01 for it firing live.
 *
 * ANNOTATIONS vs hbm.xml, what changed here:
 *   - cascade="save-update" (hbm) has no single JPA CascadeType equivalent.
 *     save-update roughly means "persist new, and propagate updates" - the
 *     closest JPA combination is {CascadeType.PERSIST, CascadeType.MERGE}.
 *     Using CascadeType.ALL instead would be WRONG here: it would also
 *     cascade REMOVE, silently deleting Departments whenever their Company
 *     is deleted - exactly the behavior this entity deliberately avoids.
 *   - cascade="all-delete-orphan" (hbm, on fleet) maps cleanly to
 *     cascade = CascadeType.ALL, orphanRemoval = true.
 */
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "registration_code", nullable = false, unique = true, length = 20)
    private String registrationCode;

    /**
     * @Embedded: folds Address's columns straight into "companies". No
     * separate table, no identity - a pure value type (the annotation
     * equivalent of hbm's <component>). @AttributeOverrides supplies the
     * actual column names, since Address itself carries none.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "address_street", length = 150)),
            @AttributeOverride(name = "city", column = @Column(name = "address_city", length = 100)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "address_postal_code", length = 20)),
            @AttributeOverride(name = "country", column = @Column(name = "address_country", length = 100))
    })
    private Address address;

    @Column(name = "founded_date")
    private LocalDate foundedDate;

    /**
     * OPTIMISTIC LOCKING. Hibernate adds "and version = ?" to every
     * UPDATE/DELETE WHERE clause and bumps the value on every successful
     * write. If another transaction changed the row in between, 0 rows
     * match and Hibernate throws StaleObjectStateException (wrapped as JPA
     * OptimisticLockException). See Demo04_OptimisticLocking.
     */
    @Version
    @Column(name = "version")
    private int version;

    /**
     * ONE-TO-MANY Company -> Department. mappedBy="company" is the
     * annotation equivalent of hbm's inverse="true": it tells JPA "I am NOT
     * the owning side of this relationship, Department.company (the
     * @ManyToOne field) is - don't issue extra SQL from this side just
     * because you see items in this Set, trust Department.company instead."
     * This avoids redundant/duplicate UPDATE statements. See THEORY.md.
     */
    @OneToMany(mappedBy = "company", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Department> departments = new HashSet<>();

    /**
     * ONE-TO-MANY Company -> Vehicle (table-per-concrete-class hierarchy).
     * Here we DO use cascade=ALL + orphanRemoval=true: a company's fleet is
     * considered fully owned by the company (contrast with departments
     * above, on purpose, to show both cascading styles side by side).
     */
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
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
