package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Employee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Table-per-subclass leaf: own table "credit_card_payments", joined to "payments" on PK. */
@Entity
@Table(name = "credit_card_payments")
@PrimaryKeyJoinColumn(name = "payment_id")
public class CreditCardPayment extends Payment {

    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    @Column(name = "card_type", length = 30)
    private String cardType;

    public CreditCardPayment() {
    }

    public CreditCardPayment(BigDecimal amount, LocalDate paymentDate, Employee employee,
                              String cardLast4, String cardType) {
        super(amount, paymentDate, employee);
        this.cardLast4 = cardLast4;
        this.cardType = cardType;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
}
