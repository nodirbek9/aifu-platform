---
name: architecture
description: Module boundaries and layering in the AIFU backend. Load before adding a module, moving code between modules, or relaxing a dependency rule.
---
# Architecture rules

Modular monolith (ADR-001). Two deployables: `backend/api`, `backend/execution-worker`.

## Module layout — copy this exactly
```
uz.aifu.lms.<module>.api           controllers + DTO records (+ springdoc annotations)
uz.aifu.lms.<module>.app           application services (@Transactional), validators, ports, mappers
uz.aifu.lms.<module>.domain        entities, enums, value objects  — NO Spring imports
uz.aifu.lms.<module>.persistence   Spring Data repositories, JPQL/native queries
```
Registered modules: `common iam academics curriculum enrollment progress assessment coding
execution grading files notification analytics audit search`.

## Hard rules (ArchUnit enforces all of these)
1. Layers depend **inward** only: `api → app → domain`; `persistence → domain`.
   `domain` imports nothing from the other three.
2. A module **never** imports another module's `persistence` package or another module's JPA
   entity in order to write it. Read-only use of another module's `domain` value objects/enums
   is allowed.
3. Cross-module work goes through the owning module's `app` service interface.
4. No cycles between modules. Allowed direction is the arrow set in
   `docs/architecture/README.md`.
5. `common` depends on no module. Everything may depend on `common`.
6. Controllers never inject a repository. Services never take `HttpServletRequest`.
7. `@Transactional` only in `app`. Never on a controller, never on a repository.

## Adding a module
1. Create the 4 packages. 2. Add the name to `ArchitectureBoundaryTest.MODULES`.
3. Add it to the diagram in `docs/architecture/README.md` with its allowed arrows.
4. Migrations for its tables go in a new `V<n>__` file. 5. One `@ConfigurationProperties`
record if it needs config — no `@Value` scattered around.

## When you think you need to break a boundary
You almost certainly need an application-service method on the other module, or a read model.
If you genuinely need a new dependency arrow, write an ADR. Do not silently delete an ArchUnit
rule — that test is the only thing holding the architecture up.
