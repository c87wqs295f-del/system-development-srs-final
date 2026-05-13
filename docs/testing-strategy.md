# Testing Strategy

This document explains what the test suite covers, what it deliberately does
not cover, and why — useful both for the OOAD document and as a talking point
in the grading interview.

## Three-tier strategy

Tests are organized by the architectural layer they exercise. Each tier uses
a different collaborator for everything *below* it, so a failure in one tier
isolates the bug to that layer.

| Tier         | Location                       | Uses real              | Uses fake                |
|--------------|--------------------------------|------------------------|--------------------------|
| Domain       | `test/.../domain/`             | Nothing else           | —                        |
| Service      | `test/.../service/`            | Domain                 | In-memory repositories   |
| Integration  | `test/.../repository/`         | Domain + SQLite + JDBC | —                        |

The in-memory repositories used by the service tests are only ~30 lines each
and realize the same `UserRepository` / `RequestRepository` interfaces as
SQLite. This is the cleanest possible demonstration that Dependency Inversion
pays off: the service layer literally cannot tell which implementation it is
running against.

## What is covered

**Domain (`RequestTest`, `PriorityTest`)**
- Fresh request starts in `NEW`.
- Blank title is rejected by `Request`'s own validation.
- Full happy-path lifecycle: `NEW → ASSIGNED → IN_PROGRESS → WAITING_FOR_INFO
  → IN_PROGRESS → RESOLVED → CLOSED`.
- `CLOSED` is terminal.
- Skipping states (`NEW → IN_PROGRESS`) is rejected.
- `RESOLVED` can be reopened to `IN_PROGRESS`.
- `assign` rejected when status is already past `ASSIGNED`.
- SLA deadline = `createdAt + priority.slaHours`.
- `isOverdue` is false for a fresh request.
- `Priority` SLA constants match design (4h / 24h / 72h / 168h).

**Service (`AuthServiceTest`, `RequestServiceTest`)**
- Login: happy path, wrong password, unknown user, nulls.
- createRequest: only employees, category/priority required.
- assignRequest: managers, agents self-assigning, rejected for stranger agents
  and employees, rejected if target is not an agent.
- transitionStatus: assignee can progress and resolve, submitter can close,
  strangers blocked, managers can do anything legal, illegal lifecycle
  transitions bubble up.
- addComment: submitter, assignee, manager allowed; other employees blocked;
  empty text rejected.
- changePriority: managers only.
- listFor: manager sees all, employee sees own, agent sees assigned.
- getVisible: rejects cross-employee snooping.

**Integration (`SqliteRepositoryIntegrationTest`)**
- User subclass round-trips through the role discriminator.
- Full-field round-trip of a mid-lifecycle request including assignee,
  timestamps, and comments.
- `findByAssignee` returns only that agent's requests.
- `findByStatus` filters correctly.

## What is deliberately not covered

**HTTP layer / end-to-end UI.** The web layer is thin glue over the service
layer. Adding Selenium-style UI tests would add complexity and dependency
weight far out of proportion to a student project. Manual demo coverage is
sufficient, and the service tests already prove every rule the UI exposes.

**Password hashing.** The Phase-1 scope decision was to keep passwords in
plain text, so testing a hashing function is outside scope. The upgrade path
(swap one line in `AuthService`) has no behavioral surface to test differently.

**Performance and load.** SQLite + a handful of users is not going to hit any
performance cliff worth testing for.

**Concurrency.** A single user operating the UI at a time is the only realistic
demo scenario. The SQLite connection is the synchronization point.

## Running the suite

```bash
mvn test
```

Expected result at the time of writing: **~40 test methods**, all green,
running in under 3 seconds on a modern laptop.
