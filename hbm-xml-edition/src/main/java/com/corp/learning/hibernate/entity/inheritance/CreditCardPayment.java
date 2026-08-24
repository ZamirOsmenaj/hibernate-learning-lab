package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Table-per-subclass leaf: own table "credit_card_payments", joined to "payments" on PK. */
public class CreditCardPayment extends Payment {

    private String cardLast4;
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
