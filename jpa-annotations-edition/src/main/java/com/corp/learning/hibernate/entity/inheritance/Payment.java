package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Employee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Base of the TABLE PER SUBCLASS inheritance hierarchy. Concrete
 * subclasses: {@link CreditCardPayment}, {@link BankTransferPayment}.
 *
 * @Inheritance(JOINED) is the annotation equivalent of hbm's
 * &lt;joined-subclass&gt;. Unlike Employee's single-table strategy, here
 * EVERY class in the hierarchy gets its own table:
 *   payments               (id, amount, payment_date, employee_id)   <- base
 *   credit_card_payments   (payment_id PK/FK, card_last4, card_type)
 *   bank_transfer_payments (payment_id PK/FK, iban, bank_name)
 *
 * Loading a CreditCardPayment issues a JOIN between payments and
 * credit_card_payments (via @PrimaryKeyJoinColumn on the subclass, the
 * default column name "id" - overridden to "payment_id" there to match the
 * hbm edition's schema exactly). Loading the base type Payment
 * polymorphically ("from Payment") issues an OUTER JOIN across all
 * subclass tables.
 */
@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.JOINED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
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
