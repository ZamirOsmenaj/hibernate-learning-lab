package com.corp.learning.hibernate.entity;

import com.corp.learning.hibernate.entity.component.Address;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base of the SINGLE TABLE inheritance hierarchy. Concrete subclasses:
 * {@link Manager}, {@link Developer}.
 *
 * @Inheritance(SINGLE_TABLE) + @DiscriminatorColumn is the annotation
 * equivalent of hbm's &lt;subclass&gt; + &lt;discriminator&gt;: ONE physical
 * table "employees" holds base Employee rows AND every Manager/Developer
 * row. Columns that only make sense for a subclass (teamSize,
 * programmingLanguage) are simply NULL on rows of the other subtype.
 * Employee is NOT abstract, so plain Employee rows (e.g. an HR staff member
 * who is neither Manager nor Developer) are also allowed, stored with
 * employee_type = 'EMPLOYEE' (@DiscriminatorValue below).
 *
 * Notable property mappings demonstrated here (annotation vs hbm.xml):
 *   - email           -> @NaturalId (Hibernate extension - JPA has no
 *                        standard natural-id concept)
 *   - hireDate         -> @Column(updatable = false), same as update="false"
 *   - annualSalary     -> @Formula (Hibernate extension) + @Access(FIELD)
 *   - department       -> @ManyToOne(EAGER) + @Fetch(JOIN)
 *   - certifications  -> @ElementCollection of plain Strings, unordered
 *                        (no @OrderColumn) = the annotation equivalent of &lt;bag&gt;
 *   - projects         -> @ManyToMany, owning side, @Fetch(SUBSELECT)
 *   - employeeProfile  -> @OneToOne, PK-to-PK association via @MapsId
 *                        on the EmployeeProfile side (see that class)
 */
@Entity
@Table(name = "employees")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "employee_type", discriminatorType = DiscriminatorType.STRING, length = 20)
@DiscriminatorValue("EMPLOYEE")
@BatchSize(size = 10)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * NATURAL-ID: an alternate business key besides the surrogate id.
     * @NaturalId is a Hibernate-specific extension - JPA has no standard
     * equivalent. Lets Hibernate look entities up efficiently by natural id
     * (session.byNaturalId) and documents "this is what uniquely identifies
     * an Employee in the real world" independent of the technical PK.
     * mutable = true because, unlike a real natural key, we do allow the
     * email to be changed later.
     */
    @NaturalId(mutable = true)
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    @Column(name = "salary", nullable = false)
    private BigDecimal salary;

    /**
     * updatable = false: once inserted, this column is never included in an
     * UPDATE statement again, even if the Java field is changed and the
     * entity is flushed - it is effectively immutable after creation.
     * Direct equivalent of hbm's update="false". Good fit for audit-style
     * "when did this happen" columns.
     */
    @Column(name = "hire_date", nullable = false, updatable = false)
    private LocalDate hireDate;

    /**
     * @Formula (org.hibernate.annotations, NOT part of JPA): NOT a real
     * column. On every SELECT, Hibernate substitutes this SQL expression
     * instead of a column reference. Never appears in INSERT/UPDATE.
     *
     * @Access(AccessType.FIELD): Employee.annualSalary intentionally has a
     * getter but NO setter (see below - "no setter on purpose"). Without
     * this, Hibernate's default property (getter/setter) access strategy
     * would require a setter to exist for EVERY mapped property, even a
     * formula it never actually writes to - the exact same
     * PropertyNotFoundException bug we hit and fixed in the .hbm.xml
     * edition (on the Manager/Developer subclass persisters, since they
     * inherit this property). @Access(FIELD) makes Hibernate read/write
     * the private field directly instead of hunting for a setter, so the
     * missing setter is no longer a problem - the public getter below is
     * still there for your own Java code to call as normal.
     */
    @Formula("(salary * 12)")
    @Access(AccessType.FIELD)
    private BigDecimal annualSalary;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "address_street", length = 150)),
            @AttributeOverride(name = "city", column = @Column(name = "address_city", length = 100)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "address_postal_code", length = 20)),
            @AttributeOverride(name = "country", column = @Column(name = "address_country", length = 100))
    })
    private Address address;

    /**
     * MANY-TO-ONE Employee -> Department, EAGER + @Fetch(JOIN): every time
     * an Employee is loaded, Hibernate issues a SQL JOIN to departments and
     * populates it eagerly in the SAME query - no extra round trip, but
     * always pays the join cost even when you never touch getDepartment().
     * Contrasts with Department.company (LAZY, no @Fetch override) and
     * Project.employees (@Fetch(SUBSELECT)) - see Demo05_FetchingStrategies
     * for all three side by side.
     *
     * @Fetch(FetchMode.JOIN) is a Hibernate-specific extension; plain JPA's
     * FetchType.EAGER alone leaves the actual SQL strategy vendor-defined
     * (Hibernate's own default for eager @ManyToOne already happens to be a
     * join, but this makes the intent explicit and self-documenting).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /**
     * ONE-TO-ONE Employee -> EmployeeProfile, PK-to-PK association.
     * mappedBy = "employee": Employee is NOT the owning side here -
     * EmployeeProfile is, via @MapsId (see EmployeeProfile.java for the
     * full explanation, and why @MapsId only belongs on that side, not
     * here - the exact same lesson as the constrained="true" bug we fixed
     * in the .hbm.xml edition's Employee.hbm.xml).
     */
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EmployeeProfile employeeProfile;

    /**
     * @ElementCollection of plain Strings with NO @OrderColumn: unordered,
     * DUPLICATES ALLOWED, no uniqueness constraint - the direct annotation
     * equivalent of hbm's &lt;bag&gt;. Same trade-off as the XML edition:
     * because a bag/unordered element collection has no way to identify
     * "this one row" cheaply, Hibernate always deletes ALL rows and
     * re-inserts on any change.
     */
    @ElementCollection
    @CollectionTable(name = "employee_certifications", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "certification_name", length = 150)
    private List<String> certifications = new ArrayList<>();

    /**
     * MANY-TO-MANY Employee &lt;-&gt; Project. This side OWNS the join
     * table "employee_project" (no mappedBy here - the default/owning
     * side, same as hbm's inverse="false"). @Fetch(SUBSELECT): when
     * multiple Employees' projects collections need initializing in the
     * same session, Hibernate re-runs the ORIGINAL employee query as a
     * subselect to fetch ALL of their projects in ONE extra query instead
     * of one query per employee.
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "employee_project",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    @Fetch(FetchMode.SUBSELECT)
    private Set<Project> projects = new HashSet<>();

    @Version
    @Column(name = "version")
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
