package com.corp.learning.hibernate.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Concrete subclass in the SINGLE TABLE (discriminator) strategy.
 * Row in "employees" with employee_type = 'DEVELOPER'.
 */
@Entity
@DiscriminatorValue("DEVELOPER")
public class Developer extends Employee {

    @Column(name = "programming_language", length = 50)
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
