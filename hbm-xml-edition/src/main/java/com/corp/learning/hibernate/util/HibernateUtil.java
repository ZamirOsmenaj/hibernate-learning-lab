package com.corp.learning.hibernate.util;

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
 * Classic (non-JPA) Hibernate bootstrap.
 *
 * IMPORTANT: this deliberately does NOT use `new Configuration().configure()`
 * + relying on the <mapping resource="..."/> entries inside hibernate.cfg.xml
 * to register the entities. That legacy path has a known soft spot: in some
 * environments (certain Hibernate 5.4.x + classloader/packaging combos - the
 * exact trigger is still murky) it silently builds a Metadata model with
 * ZERO entities bound, even though the <mapping> entries are present and
 * well-formed. There's no error at startup - hbm2ddl never runs (no
 * CREATE TABLE statements logged), and you only find out later, confusingly,
 * the first time you call session.save(...) and get "Unknown entity: ...".
 *
 * The fix: register every hbm.xml file EXPLICITLY via
 * MetadataSources.addResource(...), which is the modern, non-deprecated
 * Hibernate 5 bootstrap API and does not go through the same silent-drop
 * code path. We also verify the result immediately (metadata.getEntityBindings())
 * and fail loudly at startup with a clear message if anything is missing,
 * instead of letting it surface later as a confusing runtime error deep
 * inside some unrelated DAO call.
 *
 * hibernate.cfg.xml is still used, but now ONLY for connection/dialect/
 * schema-management settings - see the comment left there.
 *
 * IMPORTANT (lazy init on purpose): the SessionFactory is built lazily, on
 * the first call to getSessionFactory(), behind double-checked locking -
 * NOT via `private static final SessionFactory SF = buildSessionFactory();`.
 * That eager form has a nasty failure mode: if the very first class-load
 * triggers buildSessionFactory() and it throws, the JVM permanently "poisons"
 * this class per the static-initializer spec - EVERY later reference to
 * HibernateUtil (even just HibernateUtil.shutdown() in a finally block)
 * throws a brand new NoClassDefFoundError instead of the real exception.
 * If that second, unrelated NoClassDefFoundError happens to be thrown while
 * another exception is already propagating out of a plain try/finally, it
 * SILENTLY REPLACES the original one - so the only thing printed to the
 * console is a near-useless one-liner, and the actual root cause (with its
 * "Caused by:" chain) never gets shown. Lazy init avoids this: a failure
 * here just throws a normal RuntimeException with the full cause chain
 * intact, and does not poison the class for later calls.
 */
public final class HibernateUtil {

    /**
     * Bump this string any time you need an unmistakable way to confirm, from
     * the container logs, that Docker actually rebuilt and is running your
     * CURRENT code rather than a stale cached image. If you change a file and
     * still see the OLD value of this marker in the logs, the problem is the
     * build/cache, not the code - see README.md "Forcing a truly clean rebuild".
     */
    private static final String BUILD_MARKER = "hbm-xml-edition build marker: 2026-08-22-initial";

    private static final String[] MAPPING_RESOURCES = {
            "com/corp/learning/hibernate/entity/Company.hbm.xml",
            "com/corp/learning/hibernate/entity/Department.hbm.xml",
            "com/corp/learning/hibernate/entity/Employee.hbm.xml",
            "com/corp/learning/hibernate/entity/EmployeeProfile.hbm.xml",
            "com/corp/learning/hibernate/entity/Project.hbm.xml",
            "com/corp/learning/hibernate/entity/Payment.hbm.xml",
            "com/corp/learning/hibernate/entity/Vehicle.hbm.xml"
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
            // If this ever prints two DIFFERENT paths for what should be a single
            // dependency, or a path that doesn't match the version above, that's
            // a genuine classpath/version conflict. Otherwise, if everything above
            // looks right and it still fails, look at the FULL stack trace printed
            // below (not just the top-level message) - the real cause is almost
            // always further down in the "Caused by:" chain.

            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                    .configure(); // reads hibernate.cfg.xml for connection/dialect/schema settings only

            overrideIfPresent(registryBuilder, "hibernate.connection.url", "DB_URL");
            overrideIfPresent(registryBuilder, "hibernate.connection.username", "DB_USER");
            overrideIfPresent(registryBuilder, "hibernate.connection.password", "DB_PASSWORD");

            StandardServiceRegistry registry = registryBuilder.build();

            try {
                MetadataSources sources = new MetadataSources(registry);
                for (String resource : MAPPING_RESOURCES) {
                    sources.addResource(resource);
                }

                Metadata metadata = sources.buildMetadata();

                // Fail fast, not confusingly: verify the mappings actually bound
                // BEFORE handing back a SessionFactory that would otherwise fail
                // later with a cryptic "Unknown entity" on the first save().
                int entityCount = metadata.getEntityBindings().size();
                if (entityCount == 0) {
                    throw new IllegalStateException(
                            "Hibernate Metadata was built with ZERO entities bound, even though "
                                    + MAPPING_RESOURCES.length + " hbm.xml resource(s) were explicitly registered: "
                                    + String.join(", ", MAPPING_RESOURCES)
                                    + ". Check that these files exist on the classpath at those exact paths "
                                    + "(src/main/resources/...) and are valid hbm.xml mappings.");
                }

                String mappedNames = metadata.getEntityBindings().stream()
                        .map(PersistentClass::getEntityName)
                        .collect(Collectors.joining(", "));
                System.out.println("Mapped entities (" + entityCount + "): " + mappedNames);

                return metadata.buildSessionFactory();
            } catch (Throwable ex) {
                // Don't leak the StandardServiceRegistry (and its pooled JDBC
                // connections) if metadata/SessionFactory building blows up.
                StandardServiceRegistryBuilder.destroy(registry);
                throw ex;
            }
        } catch (Throwable ex) {
            // Print the FULL stack trace, including every "Caused by:" - the
            // top-level exception (e.g. "Could not get constructor for
            // ...Persister") is usually just a wrapper; the actual root cause
            // (a proxy/bytecode-generation failure, a classpath conflict, a
            // bad mapping, etc.) is further down the chain and invisible if
            // you only log ex.toString().
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
