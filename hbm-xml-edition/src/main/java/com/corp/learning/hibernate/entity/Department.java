package com.corp.learning.hibernate.entity;

import com.corp.learning.hibernate.entity.component.BudgetEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Department belongs to a Company (many-to-one, the OWNING side of that
 * association - it holds the foreign key column company_id).
 *
 * Department is the INVERSE side of Company.departments (inverse="true" in
 * the XML) - meaning Hibernate ignores Department objects being merely
 * present in Company.getDepartments() for the purpose of deciding what SQL
 * to issue; it looks at Department.company instead. See THEORY.md "inverse"
 * section and Demo06_InverseVsNonInverse.
 *
 * Also demonstrates three different collection-mapping styles side by side:
 *   - employees   -> <set>   of entities (one-to-many)
 *   - budgetHistory -> <list> of <composite-element> (ordered, value type)
 *   - tags        -> <map>  of simple key/value strings (value type)
 */
public class Department {

    private Long id;
    private String name;

    private Company company;

    private Set<Employee> employees = new HashSet<>();

    /** Ordered history, index tracked by a dedicated position column -> <list> */
    private List<BudgetEntry> budgetHistory = new ArrayList<>();

    /** Free-form key/value attributes -> <map> */
    private Map<String, String> tags = new HashMap<>();

    private int version;

    public Department() {
    }

    public Department(String name) {
        this.name = name;
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    public List<BudgetEntry> getBudgetHistory() {
        return budgetHistory;
    }

    public void setBudgetHistory(List<BudgetEntry> budgetHistory) {
        this.budgetHistory = budgetHistory;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setDepartment(this);
    }

    @Override
    public String toString() {
        return "Department{id=" + id + ", name='" + name + "'}";
    }
}
