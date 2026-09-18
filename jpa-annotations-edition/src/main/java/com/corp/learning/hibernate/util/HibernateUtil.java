package com.corp.learning.hibernate.util;

import com.corp.learning.hibernate.entity.Company;
import com.corp.learning.hibernate.entity.Department;
import com.corp.learning.hibernate.entity.Developer;
import com.corp.learning.hibernate.entity.Employee;
import com.corp.learning.hibernate.entity.EmployeeProfile;
import com.corp.learning.hibernate.entity.Manager;
import com.corp.learning.hibernate.entity.Project;
import com.corp.learning.hibernate.entity.inheritance.BankTransferPayment;
import com.corp.learning.hibernate.entity.inheritance.Car;
import com.corp.learning.hibernate.entity.inheritance.CreditCardPayment;
import com.corp.learning.hibernate.entity.inheritance.Payment;
import com.corp.learning.hibernate.entity.inheritance.Truck;
import com.corp.learning.hibernate.entity.inheritance.Vehicle;
import org.hibernate.SessionFactory;
import org.hibernate.Version;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.PersistentClass;

import java.security.CodeSource;
import java.util.stream.Collectors;

/**
 * Classic (non-JPA-EntityManager) Hibernate bootstrap - same Session/
 * Transaction API as the .hbm.xml edition, so the demo classes are nearly
 * identical between editions. The ONLY thing that changes here vs that
 * edition's HibernateUtil is HOW entities are registered:
 *
 *   .hbm.xml edition: sources.addResource("com/.../Employee.hbm.xml")
 *   this edition:     sources.addAnnotatedClass(Employee.class)
 *
 * Everything else - the "why lazy init", "why explicit registration instead
 * of relying on hibernate.cfg.xml", "why print the full stack trace" - is
 * identical reasoning to the other edition; see its HibernateUtil.java for
 * the full write-up if you haven't read it yet.
 *
 * hibernate.cfg.xml is still used, but ONLY for connection/dialect/
 * schema-management settings, same as the other edition.
 */
public final class HibernateUtil {

    /**
     * Bump this string any time you need an unmistakable way to confirm, from
     * the container logs, that Docker actually rebuilt and is running your
     * CURRENT code rather than a stale cached image.
     */
    private static final String BUILD_MARKER = "jpa-annotations-edition build marker: 2026-08-22-initial";

    private static final Class<?>[] ANNOTATED_CLASSES = {
            Company.class,
            Department.class,
            Employee.class,
            Manager.class,
            Developer.class,
            EmployeeProfile.class,
            Project.class,
            Payment.class,
            CreditCardPayment.class,
            BankTransferPayment.class,
            Vehicle.class,
            Car.class,
            Truck.class
    };

    private static volatile SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    sessionFactory = buildSessionFactory();
                }
            }
        }
        return sessionFactory;
    }

    private static SessionFactory buildSessionFactory() {
        try {
            System.out.println(BUILD_MARKER);
            System.out.println("Hibernate version: " + Version.getVersionString());
            System.out.println("hibernate-core loaded from: " + jarLocationOf(SessionFactory.class));

            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                    .configure(); // reads hibernate.cfg.xml for connection/dialect/schema settings only

            overrideIfPresent(registryBuilder, "hibernate.connection.url", "DB_URL");
            overrideIfPresent(registryBuilder, "hibernate.connection.username", "DB_USER");
            overrideIfPresent(registryBuilder, "hibernate.connection.password", "DB_PASSWORD");

            StandardServiceRegistry registry = registryBuilder.build();

            try {
                MetadataSources sources = new MetadataSources(registry);
                for (Class<?> annotatedClass : ANNOTATED_CLASSES) {
                    sources.addAnnotatedClass(annotatedClass);
                }

                Metadata metadata = sources.buildMetadata();

                int entityCount = metadata.getEntityBindings().size();
                if (entityCount == 0) {
                    throw new IllegalStateException(
                            "Hibernate Metadata was built with ZERO entities bound, even though "
                                    + ANNOTATED_CLASSES.length + " @Entity class(es) were explicitly registered. "
                                    + "Check that every class above is actually annotated with @Entity.");
                }

                String mappedNames = metadata.getEntityBindings().stream()
                        .map(PersistentClass::getEntityName)
                        .collect(Collectors.joining(", "));
                System.out.println("Mapped entities (" + entityCount + "): " + mappedNames);

                return metadata.buildSessionFactory();
            } catch (Throwable ex) {
                StandardServiceRegistryBuilder.destroy(registry);
                throw ex;
            }
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed. Full stack trace follows:");
            ex.printStackTrace();
            throw new RuntimeException("Initial SessionFactory creation failed: " + ex, ex);
        }
    }

    private static String jarLocationOf(Class<?> type) {
        try {
            CodeSource codeSource = type.getProtectionDomain().getCodeSource();
            return codeSource != null ? codeSource.getLocation().toString() : "(unknown - no CodeSource)";
        } catch (Throwable t) {
            return "(unable to determine: " + t + ")";
        }
    }

    private static void overrideIfPresent(StandardServiceRegistryBuilder registryBuilder,
                                           String propertyKey, String envVar) {
        String value = System.getenv(envVar);
        if (value != null && !value.trim().isEmpty()) {
            registryBuilder.applySetting(propertyKey, value);
        }
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
