# Hibernate Learning Lab - JPA Annotations Edition

The exact same domain model and the exact same six demos as
[`../hbm-xml-edition`](../hbm-xml-edition), mapped with JPA/Hibernate
**annotations** (`@Entity`, `@OneToMany`, `@Embeddable`, `@Inheritance`, ...)
instead of `.hbm.xml` mapping files.

If you haven't read the other edition's README/THEORY.md yet, start there -
this README assumes you already know the domain model and the six demo
scenarios, and focuses on what's *different* here.

## Quick start

```bash
docker compose up --build
```

That builds the image, starts a fresh Postgres container, waits for it to be
ready, then runs all six demos against it, printing every SQL statement
Hibernate generates along the way.

Note: this edition's `docker-compose.yml` binds Postgres to **host port
5433**, not 5432 - so you can run this edition and `hbm-xml-edition`
side by side at the same time, if you want to compare their SQL output
directly. If you don't need that, ignore it; everything else works the same.

To force a truly clean rebuild (new base image, no cached layers):

```bash
docker compose down -v --rmi all
docker compose build --no-cache
docker compose up
```

To run a single demo instead of all six:

```bash
MAIN_CLASS=com.corp.learning.hibernate.demo.Demo04_OptimisticLocking docker compose up --build
```

Explore the database directly once the demos have run:

```bash
docker compose exec db psql -U hibernate_user -d hibernate_lab
```

## What actually changed vs hbm-xml-edition

Only two things, really:

1. **Every entity is annotated instead of described in a separate
   `.hbm.xml` file.** Same tables, same columns, same constraints - see the
   translation table below for exactly which annotation replaces which hbm
   element/attribute.
2. **`HibernateUtil` registers entities differently.** Instead of
   `sources.addResource("com/.../Employee.hbm.xml")` for each mapping file,
   it calls `sources.addAnnotatedClass(Employee.class)` for each `@Entity`
   class. Everything else about `HibernateUtil` - lazy init, explicit
   registration instead of relying on `hibernate.cfg.xml`, full stack traces
   on bootstrap failure - is identical reasoning to the other edition.

The demo classes themselves are almost byte-for-byte identical: they use
the same `Session`/`Transaction`/HQL API either way, since that runtime API
doesn't care whether the mapping came from XML or annotations. Only their
doc comments were updated to point at the new annotation locations instead
of `.hbm.xml` line numbers.

## Mapping translation table (hbm.xml -> annotations)

| .hbm.xml | Annotation | Notes |
|---|---|---|
| `<class>` | `@Entity` + `@Table(name=...)` | |
| `<id><generator class="identity"/></id>` | `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` | |
| `<id><generator class="sequence">` | `@GeneratedValue(strategy = SEQUENCE)` + `@SequenceGenerator` | Vehicle - see below |
| `<version>` | `@Version` | |
| `<property not-null="true" unique="true" length="...">` | `@Column(nullable=false, unique=true, length=...)` | |
| `<property update="false">` | `@Column(updatable = false)` | Employee.hireDate |
| `<property formula="...">` | `@Formula("...")` (Hibernate extension, `org.hibernate.annotations`) | Employee.annualSalary - see below |
| `<natural-id>` | `@NaturalId` (Hibernate extension) | Employee.email |
| `<component>` | `@Embeddable` class + `@Embedded` field + `@AttributeOverrides` | Address, BudgetEntry |
| `<many-to-one fetch="select">` (default) | `@ManyToOne(fetch = FetchType.LAZY)` | Department.company |
| `<many-to-one fetch="join">` | `@ManyToOne(fetch = FetchType.EAGER)` + `@Fetch(FetchMode.JOIN)` (Hibernate extension) | Employee.department |
| `<set inverse="true">` (one-to-many) | `@OneToMany(mappedBy = "...")` | |
| `<set inverse="false">` (many-to-many owning side) | `@ManyToMany` + `@JoinTable` (no `mappedBy`) | Employee.projects |
| `<set inverse="true">` (many-to-many mirror side) | `@ManyToMany(mappedBy = "...")` | Project.employees |
| `fetch="subselect"` | `@Fetch(FetchMode.SUBSELECT)` (Hibernate extension) | Employee.projects |
| `batch-size="10"` (class or collection) | `@BatchSize(size = 10)` (Hibernate extension) | Department, Department.employees |
| `<list>` + `<composite-element>` | `@ElementCollection` + `@OrderColumn` + `@Embeddable` | Department.budgetHistory |
| `<map>` | `@ElementCollection` + `@MapKeyColumn` | Department.tags |
| `<bag>` (plain elements) | `@ElementCollection` with a `List`, no `@OrderColumn` | Employee.certifications |
| `<one-to-one>` + `generator class="foreign"` + `constrained="true"` | `@OneToOne` + `@MapsId` (on the dependent side only) | EmployeeProfile.employee |
| `<subclass>` + `<discriminator>` (single table) | `@Inheritance(strategy = SINGLE_TABLE)` + `@DiscriminatorColumn` + `@DiscriminatorValue` | Employee/Manager/Developer |
| `<joined-subclass>` | `@Inheritance(strategy = JOINED)` + `@PrimaryKeyJoinColumn` on subclasses | Payment/CreditCardPayment/BankTransferPayment |
| `<union-subclass>` + `abstract="true"` | `@Inheritance(strategy = TABLE_PER_CLASS)` on an `abstract` Java class | Vehicle/Car/Truck |

Two entries are worth calling out specifically, since they're the same two
real bugs we tracked down and fixed in the `.hbm.xml` edition, and the
annotation edition has the exact same failure mode if you get them wrong:

- **`Employee.annualSalary`**: it's `@Formula`-mapped (derived, computed by
  the database) and intentionally has no setter. Without
  `@Access(AccessType.FIELD)` on that field, Hibernate's default
  getter/setter reflection would demand a setter that was never supposed to
  exist, and fail at SessionFactory bootstrap with a
  `PropertyNotFoundException` - building the `Manager`/`Developer`
  persisters, since they inherit the property.
- **`EmployeeProfile.employee`**: `@MapsId` (the annotation equivalent of
  hbm's `constrained="true"` + `generator class="foreign"`) belongs *only*
  on the dependent side (`EmployeeProfile`, whose primary key is derived
  from `Employee`'s). Putting it on `Employee` as well would claim the
  dependency runs the other way around.

## A trap that's unique to this edition: session.save() vs session.persist()

This one has no equivalent in `hbm-xml-edition` at all, and it's the kind of
thing that fails *silently* rather than with a clear exception, so it's
worth understanding even though the demo code is already fixed.

`Company.departments` is mapped `cascade = {CascadeType.PERSIST, CascadeType.MERGE}`
- those are **JPA** cascade types, and they're only wired to **JPA-style**
operations (`session.persist()`/`session.merge()`, or an `EntityManager`'s
`persist()`/`merge()`). Hibernate's own *native* `session.save()` and
`session.saveOrUpdate()` predate JPA cascade types entirely and use a
separate, older cascading mechanism of their own (Hibernate's native
`"save-update"` cascade style - the same one `hbm-xml-edition`'s
`cascade="save-update"` maps to directly).

Call `session.save(company)` here instead of `session.persist(company)` and
you won't get an error: the `Company` row inserts just fine, and the
`Department`s **silently never get cascaded at all** - no exception, just
missing rows, discovered confusingly later (e.g. a query for a Department
you were sure you just created coming back `null`). Every demo in this
edition uses `session.persist(...)` for exactly this reason wherever it
relies on `CascadeType.PERSIST`/`MERGE` to propagate a save.

## Running against a local Postgres instead of Docker

Same as the other edition: set `DB_URL`, `DB_USER`, `DB_PASSWORD` as
environment variables before running the jar or an IDE-launched demo class;
`HibernateUtil` picks them up over the `hibernate.cfg.xml` defaults if
present.
