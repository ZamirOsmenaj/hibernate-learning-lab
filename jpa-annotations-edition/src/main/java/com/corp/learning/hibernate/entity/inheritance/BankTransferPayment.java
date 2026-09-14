package com.corp.learning.hibernate.entity.inheritance;

import com.corp.learning.hibernate.entity.Employee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Table-per-subclass leaf: own table "bank_transfer_payments", joined to "payments" on PK. */
@Entity
@Table(name = "bank_transfer_payments")
@PrimaryKeyJoinColumn(name = "payment_id")
public class BankTransferPayment extends Payment {

    @Column(name = "iban", length = 34)
    private String iban;

    @Column(name = "bank_name", length = 100)
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
