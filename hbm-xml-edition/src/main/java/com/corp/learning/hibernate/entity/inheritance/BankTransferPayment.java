package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Table-per-subclass leaf: own table "bank_transfer_payments", joined to "payments" on PK. */
public class BankTransferPayment extends Payment {

    private String iban;
    private String bankName;

    public BankTransferPayment() {
    }

    public BankTransferPayment(BigDecimal amount, LocalDate paymentDate, Employee employee,
                                String iban, String bankName) {
        super(amount, paymentDate, employee);
        this.iban = iban;
        this.bankName = bankName;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
