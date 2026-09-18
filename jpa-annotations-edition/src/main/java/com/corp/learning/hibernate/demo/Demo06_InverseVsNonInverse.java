package com.corp.learning.hibernate.demo;

import com.corp.learning.hibernate.entity.Employee;
import com.corp.learning.hibernate.entity.Project;
import com.corp.learning.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDate;

/**
 * DEMO 6 - mappedBy ("the mirror side") vs the OWNING side of an association.
 *
 * Employee.projects is the OWNING side of the employee_project join table
 * (no mappedBy in Employee.java - it declares the @JoinTable directly).
 * Project.employees only MIRRORS it (mappedBy = "projects" in Project.java).
 * Only the owning side's in-memory state is consulted when Hibernate
 * decides what INSERT/DELETE statements to issue for the join table - the
 * mappedBy side is Java-only bookkeeping. This is the exact same concept as
 * the .hbm.xml edition's inverse="true"/"false", just spelled differently.
 *
 * This demo deliberately makes the classic mistake first (writing only to
 * the mappedBy/inverse side) so you can see NOTHING gets persisted, then
 * does it correctly.
 */
public class Demo06_InverseVsNonInverse {

    public static void run() {
        SessionFactory sf = HibernateUtil.getSessionFactory();

        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();

        Project migration = new Project("Cloud Migration", LocalDate.now().plusMonths(3));
        session.persist(migration); // persist(), not save() - see Demo01 for why

        Employee bob = session.createQuery("from Employee where email = :email", Employee.class)
                .setParameter("email", "bob.smith@acme.example")
                .uniqueResult();

        // --- WRONG: only touching the mappedBy (inverse) side ---
        migration.getEmployees().add(bob);
        session.flush();
        System.out.println(">> Added Bob to Project.employees (the mappedBy/inverse side) and flushed.");
        System.out.println("   Look above: there should be NO INSERT INTO employee_project - "
                + "Hibernate ignored this change because mappedBy tells it this side"
                + " doesn't own the relationship.");

        // --- RIGHT: touching the OWNING side ---
        bob.getProjects().add(migration);
        session.flush();
        System.out.println(">> Now added the Project to Employee.projects (the OWNING side) and flushed.");
        System.out.println("   Look above: THIS time an INSERT INTO employee_project should appear.");

        tx.commit();
        session.close();

        // ---- verify from a clean session ----
        Session verify = sf.openSession();
        Employee reloadedBob = verify.createQuery("from Employee where email = :email", Employee.class)
                .setParameter("email", "bob.smith@acme.example")
                .uniqueResult();
        System.out.println(">> Reloaded Bob's projects from a fresh session: " + reloadedBob.getProjects());
        verify.close();
    }
}
