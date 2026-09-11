package com.corp.learning.hibernate.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * INVERSE side of the MANY-TO-MANY association with Employee.
 *
 * The join table "employee_project" is owned by Employee.projects
 * (inverse="false" there); Project.employees only mirrors it
 * (inverse="true" here). Only one side may own a many-to-many join table -
 * see README-hbm-xml.md.
 */
public class Project {

    private Long id;
    private String name;
    private LocalDate deadline;

    private Set<Employee> employees = new HashSet<>();

    private int version;

    public Project() {
    }

    public Project(String name, LocalDate deadline) {
        this.name = name;
        this.deadline = deadline;
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

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Project{id=" + id + ", name='" + name + "'}";
    }
}
