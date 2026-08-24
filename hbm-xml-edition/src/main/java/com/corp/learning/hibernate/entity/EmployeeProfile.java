package com.corp.learning.hibernate.entity;

/**
 * ONE-TO-ONE association with Employee, mapped as a shared primary key
 * (EmployeeProfile.id is both its own PK and a FK back to employees.id -
 * generator class="foreign" in the XML). This is the classic Hibernate way
 * to do a true 1:1 without an extra unique-key column.
 *
 * Lifecycle: cascade="all-delete-orphan" is set on the Employee side, so a
 * profile is created/updated/deleted automatically together with its owning
 * Employee - you never save/delete an EmployeeProfile directly.
 */
public class EmployeeProfile {

    private Long id;
    private String bio;
    private String linkedinUrl;

    /** Hibernate needs this back-reference to resolve the shared PK on insert. */
    private Employee employee;

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
