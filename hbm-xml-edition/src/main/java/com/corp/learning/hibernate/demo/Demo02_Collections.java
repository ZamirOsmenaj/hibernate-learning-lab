package com.corp.learning.hibernate.demo;

import com.corp.learning.hibernate.entity.Department;
import com.corp.learning.hibernate.entity.Employee;
import com.corp.learning.hibernate.entity.component.Address;
import com.corp.learning.hibernate.entity.component.BudgetEntry;
import com.corp.learning.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DEMO 2 - collection-mapping styles beyond the &lt;set&gt; you already know:
 *   - &lt;list&gt;  (Department.budgetHistory) - ordered, of &lt;composite-element&gt; value objects
 *   - &lt;map&gt;   (Department.tags)         - key/value pairs
 *   - &lt;bag&gt;   (Employee.certifications)  - unordered, duplicates allowed, plain elements
 */
public class Demo02_Collections {

    public static void run() {
        SessionFactory sf = HibernateUtil.getSessionFactory();

        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();

        Department engineering = (Department) session
                .createQuery("from Department where name = :name")
                .setParameter("name", "Engineering")
                .uniqueResult();

        // ---- <list>: ordered history of composite-elements ----
        engineering.getBudgetHistory().add(new BudgetEntry(2023, new BigDecimal("250000.00"), "Initial headcount"));
        engineering.getBudgetHistory().add(new BudgetEntry(2024, new BigDecimal("410000.00"), "Two new hires"));
        engineering.getBudgetHistory().add(new BudgetEntry(2025, new BigDecimal("530000.00"), "Cloud spend increase"));

        // ---- <map>: free-form key/value tags ----
        engineering.getTags().put("cost-center", "ENG-100");
        engineering.getTags().put("location", "Athens HQ");
        engineering.getTags().put("on-call", "true");

        // A base Employee (not Manager/Developer) with a <bag> of certifications
        Employee dana = new Employee("Dana", "Lee", "dana.lee@acme.example",
                new BigDecimal("42000.00"), LocalDate.of(2022, 6, 1));
        dana.setAddress(new Address("22 Harbor Rd", "Piraeus", "18531", "Greece"));
        engineering.addEmployee(dana);
        dana.getCertifications().add("First Aid");
        dana.getCertifications().add("GDPR Fundamentals");
        dana.getCertifications().add("GDPR Fundamentals"); // duplicates ARE allowed in a <bag>, unlike <set>
        session.save(dana);

        tx.commit();
        session.close();

        System.out.println(">> Saved list/map/bag collections. Re-loading in a fresh session to prove"
                + " they came back from the DATABASE, not from in-memory Java state:");

        Session verifySession = sf.openSession();
        Department reloadedDept = (Department) verifySession
                .createQuery("from Department where name = :name")
                .setParameter("name", "Engineering")
                .uniqueResult();

        System.out.println("   <list> budgetHistory (ORDER preserved via the 'position' column):");
        int index = 0;
        for (BudgetEntry entry : reloadedDept.getBudgetHistory()) {
            System.out.println("     [" + (index++) + "] " + entry);
        }

        System.out.println("   <map> tags:");
        for (Map.Entry<String, String> tag : reloadedDept.getTags().entrySet()) {
            System.out.println("     " + tag.getKey() + " = " + tag.getValue());
        }

        Query<Employee> employeeQuery = verifySession.createQuery(
                "from Employee where email = :email", Employee.class);
        Employee reloadedDana = employeeQuery.setParameter("email", "dana.lee@acme.example").uniqueResult();
        List<String> certs = reloadedDana.getCertifications();
        System.out.println("   <bag> certifications (size=" + certs.size()
                + ", duplicates preserved, order NOT guaranteed): " + certs);

        verifySession.close();
    }
}
