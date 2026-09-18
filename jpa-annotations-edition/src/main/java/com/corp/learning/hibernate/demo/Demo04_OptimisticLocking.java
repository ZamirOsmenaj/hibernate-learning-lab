package com.corp.learning.hibernate.demo;

import com.corp.learning.hibernate.entity.Company;
import com.corp.learning.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

import javax.persistence.OptimisticLockException;

/**
 * DEMO 4 - OPTIMISTIC LOCKING via @Version.
 *
 * Simulates the classic "two users open the same record at the same time"
 * race: two SEPARATE sessions load the same Company (same version number),
 * the first one saves its change (version bumps 0 -> 1 in the DB), then the
 * second tries to save its own change still holding the stale version=0 in
 * memory. Hibernate appends "and version = ?" to the UPDATE and, matching
 * zero rows, raises StaleObjectStateException instead of silently
 * overwriting the first user's change ("lost update" problem). Identical
 * mechanism to the .hbm.xml edition's <version> - @Version is simply the
 * annotation for the exact same Hibernate feature.
 */
public class Demo04_OptimisticLocking {

    public static void run() {
        SessionFactory sf = HibernateUtil.getSessionFactory();

        // Two independent sessions both load the SAME row, simulating two users/tabs/requests
        Session sessionUserA = sf.openSession();
        Company companyAsSeenByUserA = sessionUserA.createQuery("from Company where name = :n", Company.class)
                .setParameter("n", "Acme Corporation")
                .uniqueResult();
        System.out.println(">> User A loaded Company, version=" + companyAsSeenByUserA.getVersion());

        Session sessionUserB = sf.openSession();
        Company companyAsSeenByUserB = sessionUserB.createQuery("from Company where name = :n", Company.class)
                .setParameter("n", "Acme Corporation")
                .uniqueResult();
        System.out.println(">> User B loaded the SAME Company, version=" + companyAsSeenByUserB.getVersion());

        // User A saves first -> succeeds, version increments in the DB (e.g. 0 -> 1)
        Transaction txA = sessionUserA.beginTransaction();
        companyAsSeenByUserA.setName("Acme Corporation (HQ renamed by User A)");
        txA.commit();
        sessionUserA.close();
        System.out.println(">> User A committed successfully. New version=" + companyAsSeenByUserA.getVersion());

        // User B still holds the OLD version number and tries to save their own change
        Transaction txB = sessionUserB.beginTransaction();
        try {
            companyAsSeenByUserB.setName("Acme Corporation (renamed by User B)");
            sessionUserB.flush(); // force the UPDATE now so we can catch the failure here
            txB.commit();
            System.out.println("!! Unexpectedly succeeded - User B's stale write silently overwrote User A's change.");
        } catch (OptimisticLockException e) {
            // Same Hibernate 5.2+ ExceptionConverterImpl wrapping as the
            // .hbm.xml edition: StaleObjectStateException gets converted
            // into a JPA OptimisticLockException before it reaches this
            // catch block, regardless of XML vs annotation mapping.
            // Catching StaleObjectStateException directly would never match.
            txB.rollback();
            Throwable cause = e.getCause();
            String rootType = cause instanceof StaleObjectStateException
                    ? cause.getClass().getSimpleName() : e.getClass().getSimpleName();
            System.out.println(">> As expected: User B's UPDATE matched ZERO rows because the WHERE clause"
                    + " included \"and version = <the old value>\", which no longer matched. Hibernate refused"
                    + " to apply a stale write and threw: " + rootType);
        } finally {
            sessionUserB.close();
        }

        // Restore the original name so later demo runs / re-runs stay predictable
        Session cleanup = sf.openSession();
        Transaction cleanupTx = cleanup.beginTransaction();
        Company toRestore = cleanup.get(Company.class, companyAsSeenByUserA.getId());
        toRestore.setName("Acme Corporation");
        cleanupTx.commit();
        cleanup.close();
    }
}
