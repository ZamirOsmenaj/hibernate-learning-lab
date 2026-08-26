package com.corp.learning.hibernate.demo;

import com.corp.learning.hibernate.entity.Company;
import com.corp.learning.hibernate.entity.Department;
import com.corp.learning.hibernate.entity.Developer;
import com.corp.learning.hibernate.entity.Employee;
import com.corp.learning.hibernate.entity.Manager;
import com.corp.learning.hibernate.entity.component.Address;
import com.corp.learning.hibernate.entity.inheritance.BankTransferPayment;
import com.corp.learning.hibernate.entity.inheritance.Car;
import com.corp.learning.hibernate.entity.inheritance.CreditCardPayment;
import com.corp.learning.hibernate.entity.inheritance.Payment;
import com.corp.learning.hibernate.entity.inheritance.Truck;
import com.corp.learning.hibernate.entity.inheritance.Vehicle;
import com.corp.learning.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DEMO 3 - the three classic Hibernate inheritance mapping strategies, side by side:
 *
 *   1) SINGLE TABLE  - Employee / Manager / Developer   (&lt;subclass&gt; + &lt;discriminator&gt;)
 *   2) JOINED        - Payment / CreditCardPayment / BankTransferPayment (&lt;joined-subclass&gt;)
 *   3) TABLE PER CLASS - Vehicle / Car / Truck          (&lt;union-subclass&gt;)
 *
 * For each we save concrete subtypes, then run a POLYMORPHIC query against the
 * BASE class and watch the generated SQL differ (single SELECT vs LEFT OUTER
 * JOIN vs UNION) in the console.
 */
public class Demo03_Inheritance {

    public static void run() {
        SessionFactory sf = HibernateUtil.getSessionFactory();

        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();

        Department engineering = (Department) session
                .createQuery("from Department where name = :name")
                .setParameter("name", "Engineering")
                .uniqueResult();
        Company acme = engineering.getCompany();

        // --- 1) SINGLE TABLE: Manager + Developer, both stored in "employees" ---
        Manager alice = new Manager("Alice", "Johnson", "alice.johnson@acme.example",
                new BigDecimal("78000.00"), LocalDate.of(2019, 4, 3), 6);
        alice.setAddress(new Address("5 Leader Ave", "Athens", "10432", "Greece"));
        engineering.addEmployee(alice);
        session.save(alice);

        Developer bob = new Developer("Bob", "Smith", "bob.smith@acme.example",
                new BigDecimal("64000.00"), LocalDate.of(2021, 1, 15), "Java");
        bob.setAddress(new Address("8 Coder St", "Athens", "10433", "Greece"));
        engineering.addEmployee(bob);
        session.save(bob);

        // --- 2) JOINED: two different Payment subtypes, each with its own table ---
        CreditCardPayment ccPayment = new CreditCardPayment(
                new BigDecimal("2500.00"), LocalDate.now(), alice, "4242", "VISA");
        BankTransferPayment btPayment = new BankTransferPayment(
                new BigDecimal("2100.00"), LocalDate.now(), bob, "GR1601101250000000012300695", "Ethniki Bank");
        session.save(ccPayment);
        session.save(btPayment);

        // --- 3) UNION-SUBCLASS: Car + Truck, two fully independent tables ---
        Car car = new Car("ATH-1234", LocalDate.of(2022, 5, 20), acme, 5);
        Truck truck = new Truck("ATH-5678", LocalDate.of(2021, 11, 2), acme, new BigDecimal("3500.00"));
        acme.getFleet().add(car);
        acme.getFleet().add(truck);
        session.save(car);
        session.save(truck);

        tx.commit();
        session.close();

        // ---- Polymorphic reads: watch the generated SQL for each strategy ----
        Session readSession = sf.openSession();

        System.out.println(">> [SINGLE TABLE] 'from Employee' -> ONE simple SELECT with a WHERE/CASE"
                + " on the discriminator, no joins needed to know the concrete type:");
        List<?> employees = readSession.createQuery("from Employee").list();
        for (Object o : employees) {
            System.out.println("     " + describeEmployee((Employee) o));
        }

        System.out.println(">> [JOINED-SUBCLASS] 'from Payment' -> base table LEFT OUTER JOINed with"
                + " EVERY subclass table so Hibernate can tell which columns to read:");
        List<?> payments = readSession.createQuery("from Payment").list();
        for (Object o : payments) {
            System.out.println("     " + describePayment((Payment) o));
        }

        System.out.println(">> [UNION-SUBCLASS] 'from Vehicle' -> SQL UNION across cars and trucks,"
                + " each already containing every inherited column:");
        List<?> vehicles = readSession.createQuery("from Vehicle").list();
        for (Object o : vehicles) {
            System.out.println("     " + o);
        }

        readSession.close();
    }

    private static String describeEmployee(Employee e) {
        if (e instanceof Manager) {
            return e + " teamSize=" + ((Manager) e).getTeamSize();
        } else if (e instanceof Developer) {
            return e + " language=" + ((Developer) e).getProgrammingLanguage();
        }
        return e + " (base Employee, no subtype)";
    }

    private static String describePayment(Payment p) {
        if (p instanceof CreditCardPayment) {
            CreditCardPayment cc = (CreditCardPayment) p;
            return cc + " card=****" + cc.getCardLast4() + " (" + cc.getCardType() + ")";
        } else if (p instanceof BankTransferPayment) {
            BankTransferPayment bt = (BankTransferPayment) p;
            return bt + " iban=" + bt.getIban();
        }
        return p.toString();
    }
}
