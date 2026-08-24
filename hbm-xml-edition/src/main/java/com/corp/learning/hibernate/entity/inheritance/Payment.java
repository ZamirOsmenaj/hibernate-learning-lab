package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Base of the TABLE PER SUBCLASS inheritance hierarchy (<joined-subclass>).
 * Concrete subclasses: {@link CreditCardPayment}, {@link BankTransferPayment}.
 *
 * Unlike Employee's single-table strategy, here EVERY class in the hierarchy
 * gets its own table:
 *   payments               (id, amount, payment_date, employee_id)   <- base
 *   credit_card_payments   (payment_id PK/FK, card_last4, card_type)
 *   bank_transfer_payments (payment_id PK/FK, iban, bank_name)
 *
 * Loading a CreditCardPayment issues a JOIN between payments and
 * credit_card_payments. Loading the base type Payment polymorphically
 * (e.g. "from Payment") issues an OUTER JOIN across all subclass tables.
 *
 * Pros: normalized, subclass-only columns can be NOT NULL.
 * Cons: reads need a join (slightly slower than single-table).
 */
public class Payment {

    private Long id;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private Employee employee;

    public Payment() {
    }

    public Payment(BigDecimal amount, LocalDate paymentDate, Employee employee) {
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", amount=" + amount + "}";
    }
}
