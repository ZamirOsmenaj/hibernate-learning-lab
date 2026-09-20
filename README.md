# Hibernate Learning Lab

A hands-on Hibernate ORM lab: one realistic domain model (companies,
departments, employees, projects, payments, a vehicle fleet), mapped and run
against Postgres in Docker, with each demo isolating one specific Hibernate
concept (cascading, optimistic locking, inheritance strategies, collections,
components, etc).

This repo hosts more than one **edition** of the same lab - same domain
model and demos, different mapping approach - so they can be compared
side by side.

## Editions

| Edition | Mapping approach | Status |
|---|---|---|
| [`hbm-xml-edition/`](./hbm-xml-edition) | Classic Hibernate, `.hbm.xml` mapping files | Available |
| [`jpa-annotations-edition/`](./jpa-annotations-edition) | JPA/Hibernate annotations (`@Entity`, `@OneToMany`, ...) | Available |

Each edition is self-contained: its own `pom.xml`, `Dockerfile`,
`docker-compose.yml`, and `README.md` with edition-specific run instructions.
Start with the edition's own README for how to run it.

## Why two editions?

`.hbm.xml` mapping files are how Hibernate mappings were done for years
before JPA annotations became the norm, and plenty of legacy codebases still
use them. Seeing the exact same domain model and the exact same demos
expressed both ways is a fast way to build intuition for what annotations
are actually doing under the hood - and to recognize `.hbm.xml` mappings if
you ever have to maintain a codebase that still uses them.
