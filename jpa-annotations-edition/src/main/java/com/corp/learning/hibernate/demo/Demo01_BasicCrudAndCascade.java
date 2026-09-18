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
 * DEMO 1 - basic persistence + cascade = {PERSIST, MERGE} (the JPA-annotation
 * equivalent of hbm's cascade="save-update") + the FK-protected "you cannot
 * delete a parent while children still exist" rule.
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

        // Saving ONLY the Company is enough: cascade = {PERSIST, MERGE} on
        // Company.departments propagates the save down to both Departments.
        // NOTE: session.persist(), NOT session.save(). This matters here.
        // Company.departments is cascade = {CascadeType.PERSIST, CascadeType.MERGE} -
        // JPA cascade types, wired to JPA-style operations. Hibernate's own
        // NATIVE session.save()/saveOrUpdate() use a completely different,
        // older cascading mechanism (Hibernate's own "save-update" cascade
        // style) that CascadeType.PERSIST does NOT participate in. Calling
        // session.save(acme) here would insert the Company but SILENTLY
        // never cascade down to the Departments at all - no error, just
        // missing rows, surfacing confusingly much later (e.g. Demo02's
        // "from Department where name = 'Engineering'" returning null).
        // persist() is the operation JPA cascade types are actually wired to.
        session.persist(acme);

        tx.commit();
        session.close();
        System.out.println(">> Saved Company " + acme + " with departments " + acme.getDepartments()
                + " via a SINGLE session.persist(company) call (cascade = {PERSIST, MERGE}).");

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
            // Same Hibernate 5.2+ behavior as the .hbm.xml edition, entirely
            // independent of whether mappings come from XML or annotations:
            // SessionImpl routes every flush/commit-time failure through
            // ExceptionConverterImpl, which wraps JDBCException subtypes
            // (including ConstraintViolationException) in a JPA
            // PersistenceException before rethrowing - even in this classic,
            // non-JPA bootstrap. Catching ConstraintViolationException
            // directly here would never match.
            tx2.rollback();
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) cause;
                System.out.println(">> As expected: delete BLOCKED by the database foreign key. "
                        + "departments.company_id is NOT NULL and Company.departments does not cascade delete.");
                System.out.println("   Root SQL error: " + cve.getSQLException().getMessage().trim());
            } else {
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
        session3.persist(temp); // same reasoning as above: persist(), not save(), for cascade=PERSIST to fire

        tx3.commit();
        session3.close();

        Session session4 = sf.openSession();
        Transaction tx4 = session4.beginTransaction();
        Company tempReloaded = session4.get(Company.class, temp.getId());
        // delete children first...
        //
        // IMPORTANT: session.delete(d) alone is NOT enough. Company.departments
        // is mappedBy = "company", cascade = {PERSIST, MERGE} in Company.java.
        // mappedBy only controls which side physically writes the company_id
        // FK column - it does NOT switch cascading off. tempReloaded.getDepartments()
        // still holds a live reference to every Department we're about to
        // delete, so at flush() Hibernate cascades save/update across that
        // same collection and finds entities we just told it to delete - and
        // refuses with "deleted object would be re-saved by cascade". We
        // have to break the association (remove from the parent's
        // collection) as well, not just delete the child in isolation.
        // Iterate over a COPY of the collection since we're removing from
        // the live Set as we go.
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
