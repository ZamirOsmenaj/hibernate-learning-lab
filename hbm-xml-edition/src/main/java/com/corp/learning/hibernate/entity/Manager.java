package com.corp.learning.hibernate.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Concrete subclass in the SINGLE TABLE (discriminator) strategy.
 * Row in "employees" with employee_type = 'MANAGER'.
 */
public class Manager extends Employee {

    private Integer teamSize;

    public Manager() {
    }

    public Manager(String firstName, String lastName, String email, BigDecimal salary,
                    LocalDate hireDate, Integer teamSize) {
        super(firstName, lastName, email, salary, hireDate);
        this.teamSize = teamSize;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }
}
