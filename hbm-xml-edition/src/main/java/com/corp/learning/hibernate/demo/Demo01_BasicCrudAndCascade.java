package com.corp.learning.hibernate.demo;

import com.corp.learning.hibernate.entity.Company;
import com.corp.learning.hibernate.entity.Department;
import com.corp.learning.hibernate.entity.component.Address;
import com.corp.learning.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * DEMO 1 - basic persistence + cascade="save-update" + the FK-protected
 * "you cannot delete a parent while children still exist" rule.
 *
 * Run standalone with:
 *   mvn exec:java -Dexec.mainClass=com.corp.learning.hibernate.demo.Demo01_BasicCrudAndCascade
 */
public class Demo01_BasicCrudAndCascade {

    public static void run() {
        SessionFactory sf = HibernateUtil.getSessionFactory();

        // ---- Part A: create the PERMANENT reference data used by later demos ----
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();

        Company acme = new Company("Acme Corporation", "ACME-001",
                new Address("1 Innovation Way", "Athens", "10431", "Greece"),
                LocalDate.of(1998, 3, 12));

        Department engineering = new Department("Engineering");
        Department sales = new Department("Sales");
        acme.addDepartment(engineering); // keeps both sides of the bidirectional link in sync
        acme.addDepartment(sales);

        // Saving ONLY the Company is enough: cascade="save-update" on
        // Company.departments propagates the save down to both Departments.
        session.save(acme);

        tx.commit();
        session.close();
        System.out.println(">> Saved Company " + acme + " with departments " + acme.getDepartments()
                + " via a SINGLE session.save(company) call (cascade=\"save-update\").");

        // ---- Part B: prove that deleting a Company with existing Departments fails ----
        Session session2 = sf.openSession();
        Transaction tx2 = session2.beginTransaction();
        try {
            Company reloaded = session2.get(Company.class, acme.getId());
            System.out.println(">> Attempting to delete Company while it still has "
                    + reloaded.getDepartments().size() + " department(s)...");
            session2.delete(reloaded);
            tx2.commit();
            System.out.println("!! Unexpectedly succeeded - this should not happen with our mapping.");
        } catch (PersistenceException e) {
            // NOTE: we catch javax.persistence.PersistenceException here, NOT
            // org.hibernate.exception.ConstraintViolationException directly.
            // Since Hibernate 5.2, SessionImpl routes every flush/commit-time
            // failure through ExceptionConverterImpl, which wraps JDBCException
            // subtypes (including ConstraintViolationException) in a JPA
            // PersistenceException before rethrowing - even for this classic,
            // non-JPA bootstrap (StandardServiceRegistryBuilder/MetadataSources,
            // no EntityManagerFactory in sight). Catching ConstraintViolationException
            // directly here would simply never match, and the real exception
            // would propagate uncaught and kill the process.
            tx2.rollback();
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) cause;
                System.out.println(">> As expected: delete BLOCKED by the database foreign key. "
                        + "departments.company_id is NOT NULL and Company.departments does not cascade delete.");
                System.out.println("   Root SQL error: " + cve.getSQLException().getMessage().trim());
            } else {
                // Some other PersistenceException we didn't anticipate - don't
                // silently swallow it, rethrow so it's visible.
                throw e;
            }
        } finally {
            session2.close();
        }

        // ---- Part C: a throw-away company to show the CORRECT delete order ----
        Session session3 = sf.openSession();
        Transaction tx3 = session3.beginTransaction();

        Company temp = new Company("Temp Ventures", "TEMP-999",
                new Address("99 Pilot Street", "Patras", "26221", "Greece"), LocalDate.now());
        Department onboarding = new Department("Onboarding");
        temp.addDepartment(onboarding);
        session3.save(temp);

        tx3.commit();
        session3.close();

        Session session4 = sf.openSession();
        Transaction tx4 = session4.beginTransaction();
        Company tempReloaded = session4.get(Company.class, temp.getId());
        // delete children first...
        //
        // IMPORTANT: session.delete(d) alone is NOT enough. Company.departments
        // is inverse="true" cascade="save-update" in Company.hbm.xml. inverse
        // only controls which side physically writes the company_id FK column -
        // it does NOT switch cascading off. tempReloaded.getDepartments() still
        // holds a live reference to every Department we're about to delete, so
        // at flush() Hibernate cascades save-update across that same collection
        // and finds entities we just told it to delete - and refuses with
        // "deleted object would be re-saved by cascade". We have to break the
        // association (remove from the parent's collection) as well, not just
        // delete the child in isolation. Iterate over a COPY of the collection
        // since we're removing from the live Set as we go.
        for (Department d : new ArrayList<>(tempReloaded.getDepartments())) {
            tempReloaded.getDepartments().remove(d);
            session4.delete(d);
        }
        session4.flush();
        // ...only then the parent
        session4.delete(tempReloaded);
        tx4.commit();
        session4.close();

        System.out.println(">> Correct order (children first, then parent) deleted \"Temp Ventures\" cleanly.");
    }
}
