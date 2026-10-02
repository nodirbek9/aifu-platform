---
name: documentation
description: How to write docs, ADRs and OpenAPI here without bloating agent context.
---
# Documentation rules

## Where things go
```
CLAUDE.md                     agent contract — compact, high-value, always in context
README.md                     human onboarding
docs/architecture/            system shape, diagrams, flows
docs/domain/                  entities, invariants, lifecycles
docs/database/                schema reference, indexing, query notes
docs/api/                     endpoint contract, error model, openapi.yaml
docs/security/                auth, authorization matrix, content security, checklist
docs/code-execution/          sandbox threat model + operations
docs/development/             local setup, workflow, conventions
docs/testing/                 strategy + required coverage
docs/deployment/              runbook
docs/decisions/              ADRs — one real decision each
docs/academic-analysis/       what the syllabi said, what is pending
docs/planning/                phase plan
skills/                       task-scoped agent rules
```

## Token discipline (this is a real requirement, brief §45/§57)
- `CLAUDE.md` stays under ~250 lines. It points; it does not explain.
- A skill stays under ~120 lines and never repeats `CLAUDE.md`.
- No fact in two places. Link instead. A duplicated fact becomes a wrong fact.
- Tables over prose. Diagrams over paragraphs. Delete rather than deprecate.

## ADRs
Only for decisions with a real alternative that was really considered. Template:
Context → Options (with why each was rejected) → Decision → Consequences (good **and** bad) →
Revisit when. Never delete or rewrite an accepted ADR — supersede it and link both ways.

## OpenAPI
Annotations are the source of truth; `docs/api/openapi.yaml` is the curated hand-maintained
contract for the core flows. Regenerate the full spec with
`curl -s localhost:8080/v3/api-docs > docs/api/openapi.generated.json` and never hand-edit
the generated file.

## Honesty rules
- Mark inferences as inferred. Mark unknowns as unknown and put them in `PENDING-INPUTS.md`.
- Never document a capability that does not exist. If a phase is partial, say which parts.
- Never claim a build/test/container ran unless it ran. Write "not verified: <reason>" instead.
