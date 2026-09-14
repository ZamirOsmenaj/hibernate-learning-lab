package com.corp.learning.hibernate.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * ONE-TO-ONE association with Employee, mapped as a shared primary key
 * (employee_profiles.id is both its own PK and a FK back to employees.id).
 *
 * @MapsId is the JPA-idiomatic, annotation-based equivalent of hbm's
 * &lt;id&gt;&lt;generator class="foreign"&gt;: it tells the provider "my
 * @Id value is not independently generated - copy it from the @Id of the
 * associated Employee below". No separate unique FK column needed; the PK
 * itself IS the FK.
 *
 * IMPORTANT, and the exact annotation-based parallel to the constrained="true"
 * bug we found and fixed in the .hbm.xml edition's Employee.hbm.xml:
 * @MapsId belongs ONLY on this side - the side that actually owns/derives
 * its primary key from the other entity. Employee.employeeProfile (see
 * Employee.java) is correctly just a plain mappedBy = "employee" @OneToOne,
 * with no @MapsId and no @Id involvement at all. Putting @MapsId (or the
 * hbm equivalent, constrained="true") on the Employee side as well would
 * wrongly claim employees.id derives its value from employee_profiles.id -
 * the exact opposite of the real dependency.
 *
 * Lifecycle: cascade=ALL is set on the Employee side (see Employee.java),
 * so a profile is created/updated/deleted automatically together with its
 * owning Employee - you never save/delete an EmployeeProfile directly.
 */
@Entity
@Table(name = "employee_profiles")
public class EmployeeProfile {

    @Id
    @Column(name = "id")
    private Long id;

    /**
     * @MapsId (no argument = maps the whole @Id above from this
     * association) + @JoinColumn(name = "id"): this column is BOTH the
     * primary key of employee_profiles AND a foreign key to employees.id -
     * that dual role is exactly what @MapsId communicates to the provider.
     */
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Employee employee;

    @Column(name = "bio", length = 2000)
    private String bio;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    public EmployeeProfile() {
    }

    public EmployeeProfile(String bio, String linkedinUrl) {
        this.bio = bio;
        this.linkedinUrl = linkedinUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public String toString() {
        return "EmployeeProfile{id=" + id + ", linkedinUrl='" + linkedinUrl + "'}";
    }
}
