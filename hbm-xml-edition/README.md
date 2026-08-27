# Hibernate Learning Lab

A self-contained, corporate-style reference project for learning classic Hibernate
(native `hbm.xml` mappings, **not** JPA annotations) — relations, collection
mappings, cascades, `inverse`, inheritance strategies, optimistic locking,
components, and fetching strategies — all demonstrated with real code, real
`hbm.xml` files, and a real PostgreSQL database, fully Dockerized so you don't
need Java or Maven installed locally.

Come back to this whenever you need to check "how does X work in Hibernate again?"

## Prerequisites

Only **Docker** and **Docker Compose**. Nothing else.

## Running it

```bash
docker compose up --build
```

This will:
1. Start a PostgreSQL 15 database.
2. Build the project inside a Maven + Java 8 container (no local install needed).
3. Wipe/recreate the schema from the `hbm.xml` mappings (`hibernate.hbm2ddl.auto=create`).
4. Run all six demos in sequence, printing the generated SQL and explanations
   for each concept straight to your console.

When it's done, the containers stay up. Look through the console output — it's
designed to be read like a guided tour, not just executed once and forgotten.

To tear everything down (including the database volume, for a truly clean slate):

```bash
docker compose down -v
```

### Running a single demo

Each concept has its own demo class you can run in isolation instead of the
full suite:

```bash
docker compose run --rm -e MAIN_CLASS=com.corp.learning.hibernate.demo.Demo04_OptimisticLocking app
```

Available classes (see `src/main/java/.../demo/`):
`Demo01_BasicCrudAndCascade`, `Demo02_Collections`, `Demo03_Inheritance`,
`Demo04_OptimisticLocking`, `Demo05_FetchingStrategies`, `Demo06_InverseVsNonInverse`.

> Note: demos 2-6 assume Demo01 has already run at least once in the current
> database (it creates the baseline Company/Department data). `RunAllDemos`
> (the default) always runs them all in the correct order for you.

### Poking at the database directly

While `docker compose up` is running (or after), open a `psql` shell:

```bash
docker compose exec db psql -U hibernate_user -d hibernate_lab
```

Useful things to try:

```sql
\dt                                  -- list every table Hibernate generated
\d employees                         -- see the single-table inheritance columns
\d payments \d credit_card_payments  -- see the joined-subclass tables
\d cars \d trucks                    -- see the union-subclass tables (no "vehicles" table!)
select id, employee_type, first_name, team_size, programming_language from employees;
select * from department_budget_history order by department_id, position;
select * from employee_project;      -- the many-to-many join table
```

## Project layout

```
hbm-xml-edition/
├── docker-compose.yml            Postgres + app containers
├── Dockerfile                    Java 8 + Maven build, no local install needed
├── entrypoint.sh                 waits for DB, then runs the requested demo
├── pom.xml                       Maven build (Hibernate 5.4, Java 8)
├── THEORY.md                     <-- the concept-by-concept explanations
├── src/main/resources/
│   ├── hibernate.cfg.xml         SessionFactory config + list of all mappings
│   ├── logback.xml               logging config tuned to show SQL clearly
│   └── com/corp/learning/hibernate/entity/*.hbm.xml   <-- THE MAPPING FILES
└── src/main/java/com/corp/learning/hibernate/
    ├── entity/                   plain Java objects (POJOs), no annotations at all
    ├── entity/component/         <component> value types (Address, BudgetEntry)
    ├── entity/inheritance/       the joined-subclass and union-subclass hierarchies
    ├── util/HibernateUtil.java   SessionFactory bootstrap
    └── demo/                     six runnable, narrated demonstrations
```

## The domain model

A small "corporate" model, deliberately shaped to need every mapping style:

- **Company** (the aggregate root — protected from deletion while it has Departments)
  - has many **Department** (one-to-many)
  - has a **Vehicle** fleet (one-to-many, `union-subclass` hierarchy: **Car** / **Truck**)
  - has an embedded **Address** (`<component>`)
- **Department**
  - belongs to one **Company** (many-to-one)
  - has many **Employee** (one-to-many)
  - has an ordered **budget history** (`<list>` of `<composite-element>` `BudgetEntry`)
  - has free-form **tags** (`<map>`)
- **Employee** — root of a `subclass` + `discriminator` (single table) hierarchy
  - subtypes: **Manager**, **Developer** (plain `Employee` rows are also valid — e.g. HR staff)
  - belongs to one **Department** (many-to-one, `fetch="join"`)
  - has one **EmployeeProfile** (one-to-one, shared primary key)
  - works on many **Project** (many-to-many, owning side)
  - has **certifications** (`<bag>`)
  - has an embedded **Address** (`<component>`)
  - is paid via many **Payment** — root of a `joined-subclass` hierarchy:
    **CreditCardPayment**, **BankTransferPayment**
- **Project** — the mirror (`inverse="true"`) side of the many-to-many with Employee

## Where each requested topic lives

| Topic | Where to look |
|---|---|
| one-to-one | `Employee.hbm.xml` / `EmployeeProfile.hbm.xml` (shared PK) |
| one-to-many / many-to-one | `Company.hbm.xml` ↔ `Department.hbm.xml`, `Department.hbm.xml` ↔ `Employee.hbm.xml` |
| many-to-many | `Employee.hbm.xml` (owning) ↔ `Project.hbm.xml` (inverse) |
| `<set>` | everywhere (departments, employees, projects, fleet) |
| `<list>` | `Department.hbm.xml` → `budgetHistory` |
| `<map>` | `Department.hbm.xml` → `tags` |
| `<bag>` | `Employee.hbm.xml` → `certifications` |
| `<composite-element>` | `Department.hbm.xml` → `budgetHistory` entries (`BudgetEntry`) |
| cascade options | `Company.hbm.xml`, `Department.hbm.xml`, `Employee.hbm.xml` (all use a *different* cascade on purpose — see THEORY.md) |
| `inverse` | `Company.hbm.xml`/`Department.hbm.xml` (one-to-many) and `Employee.hbm.xml`/`Project.hbm.xml` (many-to-many) — live demo in `Demo06_InverseVsNonInverse` |
| `<property>` attributes | `Employee.hbm.xml` (`not-null`, `unique`, `length`, `update="false"`, `formula`) |
| optimistic locking (`<version>`) | every `<class>` root; live demo in `Demo04_OptimisticLocking` |
| `<component>` | `Address` (Company, Employee), `BudgetEntry` (as a `composite-element`) |
| `<subclass>` + `<discriminator>` | `Employee.hbm.xml` (Manager, Developer) |
| `<joined-subclass>` | `Payment.hbm.xml` (CreditCardPayment, BankTransferPayment) |
| `<union-subclass>` | `Vehicle.hbm.xml` (Car, Truck) |
| fetching strategies | `fetch="join"` on `Employee.department`, `fetch="select"` (default) + `batch-size` on `Department.company`, `fetch="subselect"` on `Employee.projects` — live demo in `Demo05_FetchingStrategies` |
| natural-id (bonus) | `Employee.hbm.xml` → `email` |

For the *why* behind every one of these, read **[THEORY.md](THEORY.md)** —
it explains each concept in plain language with small standalone examples,
independent of this specific project.

## If a rebuild doesn't seem to pick up your changes

Docker aggressively caches image layers. If you edit a file and the exact
same error keeps appearing, the first thing to check in the logs is the
build step itself:

```
✔ app  Built 0.0s
```

A genuine rebuild (downloading dependencies, compiling) takes well over a
few seconds — `Built 0.0s` means Docker reused an old cached image and your
container is running **stale code**, not your latest edits.

To force a truly clean rebuild:

```bash
docker compose down -v --rmi all   # removes containers, the DB volume, AND the built image
docker compose build --no-cache    # rebuilds every layer from scratch, ignoring all caches
docker compose up
```

As a sanity check, `HibernateUtil` prints a build marker line and the exact
`.jar` path Hibernate was loaded from as the very first thing at startup:

```
hbm-xml-edition build marker: 2026-08-22-initial
Hibernate version: 5.4.32.Final
hibernate-core loaded from: file:/root/.m2/repository/org/hibernate/hibernate-core/5.4.32.Final/hibernate-core-5.4.32.Final.jar
```

If that marker line ever shows an OLD value after you've edited code, you
know for certain the problem is the Docker build/cache, not the code itself
— saving you from debugging a ghost. If the `hibernate-core loaded from`
path ever looks unexpected (e.g. two different Hibernate versions resolving,
or a path outside your normal `.m2` cache), that's a real classpath conflict
worth investigating on its own.

## A note on the bootstrap approach

`HibernateUtil` builds the `SessionFactory` via `MetadataSources.addResource(...)`
in Java code (registering each `hbm.xml` explicitly and verifying at least
one entity got bound, failing loudly at startup if not) rather than via the
legacy `Configuration.configure()` + `<mapping resource="...">` XML path. The
latter has a known soft spot where it can silently build a `Metadata` model
with **zero** entities bound in some environments — no error at startup, no
`CREATE TABLE` statements, just a confusing `Unknown entity: ...` the first
time something calls `save()`. If you ever see that error in a Hibernate
project (this one or any other), this is the first thing to check.

## A note on how the app actually runs in Docker

Docker builds a runnable fat jar (`target/hbm-xml-edition.jar`, via
the `maven-shade-plugin`) at image-build time, and `entrypoint.sh` launches
it with a plain `java -cp ... <MainClass>` process — **not** `mvn exec:java`.

This is deliberate: `mvn exec:java` runs your code *inside Maven's own JVM*,
on a classloader layered on top of Maven's internal one. Hibernate's
persister factory does raw reflection (`Class.getConstructor(...)`) while
building the `SessionFactory`, and that in-process classloader setup is a
known source of confusing failures there — e.g. `MappingException: Could not
get constructor for org.hibernate.persister.entity.SingleTableEntityPersister`
— even when every dependency jar on disk is perfectly correct and
conflict-free (as you can confirm from the `hibernate-core loaded from: ...`
line `HibernateUtil` prints at startup). Running the built jar in its own
independent `java` process sidesteps that whole class of problem.

## A note on style choices

- `hibernate.hbm2ddl.auto=create` is used so every run starts from a clean,
  predictable schema — great for a learning lab, **never** do this against a
  real environment (use `validate`, and a migration tool like Flyway/Liquibase,
  instead).
- The classic `Configuration` + `hbm.xml` API is used throughout (no JPA
  `EntityManager`, no annotations) since that's what you asked to learn — it's
  also still exactly what you'll find maintaining older enterprise Java
  codebases.
- Every mapping file is heavily commented *in place* — the XML itself is
  meant to be readable as documentation, not just machine input.
