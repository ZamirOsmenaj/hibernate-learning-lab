package com.corp.learning.hibernate.entity;

import com.corp.learning.hibernate.entity.component.Address;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base of the SINGLE TABLE inheritance hierarchy (<subclass> + <discriminator>).
 * Concrete subclasses: {@link Manager}, {@link Developer}.
 *
 * All three classes share ONE physical table "employees"; a discriminator
 * column "employee_type" tells Hibernate which Java class to instantiate for
 * each row. Columns that only make sense for a subclass (teamSize,
 * programmingLanguage) are simply NULL on rows of the other subtype.
 *
 * Pros: single table = fastest reads/writes, no joins needed.
 * Cons: lots of nullable columns as the hierarchy grows; can't have
 *       NOT NULL constraints on subclass-only columns at the DB level.
 *
 * Notable property mappings demonstrated here (see Employee.hbm.xml):
 *   - email        -> <natural-id> + unique, alternate business key
 *   - hireDate     -> update="false" (immutable once inserted)
 *   - annualSalary -> formula="..." (derived/computed, no column at all)
 *   - department   -> many-to-one, fetch="join" (eager join fetch)
 *   - certifications -> <bag> of plain string elements
 *   - projects     -> many-to-many, owning side, fetch="subselect"
 *   - employeeProfile -> one-to-one, PK-to-PK association, cascade all-delete-orphan
 */
public class Employee {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal salary;
    private LocalDate hireDate;

    /** Derived, read-only value computed in the DB via a SQL formula - never written by us. */
    private BigDecimal annualSalary;

    private Address address;

    private Department department;

    private EmployeeProfile employeeProfile;

    private Set<Project> projects = new HashSet<>();

    /** Unordered, duplicates-allowed collection of plain strings -> <bag> */
    private List<String> certifications = new ArrayList<>();

    private int version;

    public Employee() {
    }

    public Employee(String firstName, String lastName, String email, BigDecimal salary, LocalDate hireDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.salary = salary;
        this.hireDate = hireDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getAnnualSalary() {
        return annualSalary;
    }

    // no setter on purpose: this is a DB-computed formula property, Hibernate never inserts/updates it

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public EmployeeProfile getEmployeeProfile() {
        return employeeProfile;
    }

    public void setEmployeeProfile(EmployeeProfile employeeProfile) {
        this.employeeProfile = employeeProfile;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void assignToProject(Project project) {
        projects.add(project);
        project.getEmployees().add(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", name='" + firstName + " " + lastName + "'}";
    }
}
