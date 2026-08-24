package com.corp.learning.hibernate.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Concrete subclass in the SINGLE TABLE (discriminator) strategy.
 * Row in "employees" with employee_type = 'DEVELOPER'.
 */
public class Developer extends Employee {

    private String programmingLanguage;

    public Developer() {
    }

    public Developer(String firstName, String lastName, String email, BigDecimal salary,
                      LocalDate hireDate, String programmingLanguage) {
        super(firstName, lastName, email, salary, hireDate);
        this.programmingLanguage = programmingLanguage;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }
}
