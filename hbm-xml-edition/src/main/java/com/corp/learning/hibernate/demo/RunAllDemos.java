package com.corp.learning.hibernate.demo;

import com.corp.learning.hibernate.util.HibernateUtil;

/**
 * Single entry point that runs every demo in a sensible order against a
 * freshly created schema (hibernate.hbm2ddl.auto=create wipes and recreates
 * everything at SessionFactory startup - see hibernate.cfg.xml).
 *
 * Docker runs this automatically. Run standalone (e.g. from your IDE, once
 * the docker-compose Postgres is up and port 5432 is reachable) with:
 *   mvn exec:java -Dexec.mainClass=com.corp.learning.hibernate.demo.RunAllDemos
 *
 * Or run any single demo directly the same way, pointing -Dexec.mainClass at
 * e.g. com.corp.learning.hibernate.demo.Demo04_OptimisticLocking.
 */
public class RunAllDemos {

    public static void main(String[] args) {
        try {
            banner("DEMO 1 / 6 - Basic CRUD, cascade=\"save-update\", and the parent-delete FK guard");
            Demo01_BasicCrudAndCascade.run();

            banner("DEMO 2 / 6 - Collection mappings: <list>, <map>, <bag>");
            Demo02_Collections.run();

            banner("DEMO 3 / 6 - Inheritance: subclass+discriminator / joined-subclass / union-subclass");
            Demo03_Inheritance.run();

            banner("DEMO 4 / 6 - Optimistic locking with <version>");
            Demo04_OptimisticLocking.run();

            banner("DEMO 5 / 6 - Fetching strategies: join / select+batch-size / subselect");
            Demo05_FetchingStrategies.run();

            banner("DEMO 6 / 6 - inverse=\"true\" vs the owning side of an association");
            Demo06_InverseVsNonInverse.run();

            banner("ALL DEMOS COMPLETE");
            System.out.println("Explore the database yourself now, e.g.:");
            System.out.println("  docker compose exec db psql -U hibernate_user -d hibernate_lab");
            System.out.println("See README.md and THEORY.md for the full write-up of everything shown above.");
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void banner(String title) {
        String line = repeat('=', Math.max(80, title.length() + 4));
        System.out.println();
        System.out.println(line);
        System.out.println("  " + title);
        System.out.println(line);
    }

    private static String repeat(char c, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
