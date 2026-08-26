package com.corp.learning.hibernate.demo;

import com.corp.learning.hibernate.entity.Department;
import com.corp.learning.hibernate.entity.Employee;
import com.corp.learning.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DEMO 5 - FETCHING STRATEGIES. Watch the console SQL output (logback prints
 * every statement Hibernate issues) while this runs; the comments below tell
 * you exactly what to look for at each step.
 *
 *   Employee.department  -> fetch="join"      (Employee.hbm.xml)
 *   Department.company   -> fetch="select"    (Department.hbm.xml, the default)
 *   Department (class)   -> batch-size="10"   (Department.hbm.xml)
 *   Employee.projects    -> fetch="subselect" (Employee.hbm.xml)
 */
public class Demo05_FetchingStrategies {

    public static void run() {
        SessionFactory sf = HibernateUtil.getSessionFactory();

        // ---- fetch="join": Employee -> Department comes back in the SAME query ----
        Session s1 = sf.openSession();
        System.out.println(">> [fetch=\"join\"] Loading Employees - expect ONE query total, with a"
                + " JOIN to departments already included (no separate SELECT for getDepartment()):");
        List<Employee> employees = s1.createQuery("from Employee", Employee.class).list();
        for (Employee e : employees) {
            // this next line touches the association, but with fetch="join" no new SQL fires here
            System.out.println("     " + e.getFirstName() + " -> department already loaded: "
                    + e.getDepartment().getName());
        }
        s1.close();

        // ---- fetch="select" (default) + batch-size on Department: N+1 turned into ceil(N/10) ----
        Session s2 = sf.openSession();
        System.out.println(">> [fetch=\"select\" + batch-size=10 on Department] Loading Departments,"
                + " then touching each one's lazy Company - expect the Company SELECTs to be BATCHED"
                + " (\"where id in (?, ?, ...)\") instead of one-by-one:");
        List<Department> departments = s2.createQuery("from Department", Department.class).list();
        for (Department d : departments) {
            System.out.println("     " + d.getName() + " belongs to company: " + d.getCompany().getName());
        }
        s2.close();

        // ---- fetch="subselect": Employee.projects loaded for ALL employees in ONE extra query ----
        Session s3 = sf.openSession();
        System.out.println(">> [fetch=\"subselect\"] Loading Employees, then touching every employee's"
                + " lazy 'projects' Set - expect exactly ONE extra query (a subselect re-running the"
                + " original employee query), not one query per employee:");
        Query<Employee> query = s3.createQuery("from Employee", Employee.class);
        List<Employee> employeesAgain = query.list();
        for (Employee e : employeesAgain) {
            System.out.println("     " + e.getFirstName() + " has " + e.getProjects().size() + " project(s)");
        }
        s3.close();

        System.out.println(">> Summary printed above; scroll up in the console to see the actual SQL"
                + " Hibernate generated for each strategy - that's the real lesson here.");
    }
}
