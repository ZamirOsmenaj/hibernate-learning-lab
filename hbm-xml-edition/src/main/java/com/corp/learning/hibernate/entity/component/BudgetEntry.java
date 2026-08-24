package com.corp.learning.hibernate.entity.component;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * VALUE TYPE used as a <composite-element> inside a collection (Department.budgetHistory).
 *
 * Difference vs Address:
 *  - Address is embedded directly into a single row of the owner's table.
 *  - BudgetEntry is embedded into EACH ROW of a separate collection table
 *    (department_budget_history), one row per list entry, because the owner
 *    can have many of these.
 *
 * Like Address, it has no identity of its own - it's identified only by its
 * position in the owning Department's list (see the <list-index> mapping).
 */
public class BudgetEntry implements Serializable {

    private Integer year;
    private BigDecimal amount;
    private String note;

    public BudgetEntry() {
    }

    public BudgetEntry(Integer year, BigDecimal amount, String note) {
        this.year = year;
        this.amount = amount;
        this.note = note;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return year + " -> " + amount + " (" + note + ")";
    }
}
