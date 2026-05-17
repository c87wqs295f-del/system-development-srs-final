# Enterprise Service Request Management System (OO Analysis & Design )

Course: Systems Development (Prof. Dr. Kai Spohrer)

---

## 1. Project Overview

### 1.1 Problem statement

A medium-sized company has no central way to manage internal service requests
(IT, Facility, HR, Supply, Access). Employees send emails that get lost;
agents can't prioritize; managers have no visibility. We build a small web
application that lets employees submit, track, and close requests, lets
agents drive them through a defined lifecycle, and lets managers oversee the
whole queue.

### 1.2 Scope

Included: user authentication (three roles), request lifecycle from
submission through closure, comments, SLA tracking per priority level,
role-based filtered views, persistent storage.

Excluded (deliberately, for scope): email notifications, file attachments,
hashed passwords, full-text search, reporting exports, external integrations.
Each exclusion is a *Phase 1 scope decision*, not a missing feature — the
design accommodates every one as a future extension without rework.

### 1.3 Actors and roles

- **Employee** — submits requests, comments on their own requests, closes a
  resolved request.
- **Service Agent** — takes ownership, progresses the status, requests more
  information, resolves.
- **Manager** — oversees everything, can reassign, change priority, and drive
  any transition the lifecycle allows.

---

## 2. Requirements Engineering

### 2.1 Functional requirements

| ID   | Requirement                                                              |
|------|--------------------------------------------------------------------------|
| FR1  | Users authenticate with a username and password.                         |
| FR2  | An employee can submit a new request with title, description, category, and priority. |
| FR3  | The system assigns an ID, timestamps, and initial status `NEW`.          |
| FR4  | A manager or agent can assign a request to a service agent.              |
| FR5  | The assignee can move the request through its lifecycle.                 |
| FR6  | The submitter can close a resolved request.                              |
| FR7  | Any authorized user can add comments.                                    |
| FR8  | A manager can change priority at any time.                               |
| FR9  | Each user sees a list filtered by role (own / assigned / all).           |
| FR10 | An SLA deadline is computed from priority and visible on each request.   |
| FR11 | Requests are persisted across restarts.                                  |

### 2.2 Non-functional requirements

| ID   | Requirement                                                              |
|------|--------------------------------------------------------------------------|
| NFR1 | Runs on a student laptop with a single command (`java -jar target/srs.jar`). |
| NFR2 | Under 10 MB of dependencies.                                             |
| NFR3 | Starts in < 2 seconds.                                                   |
| NFR4 | No external services required (self-contained SQLite file).              |
| NFR5 | Test suite runs in under 10 seconds.                                     |
| NFR6 | Code readable by any second-semester Java student.                       |

### 2.3 User stories (selection)

- As an **employee**, I want to submit a request so that the right team sees it.
- As an **employee**, I want to see only my own requests so the page is not cluttered.
- As an **agent**, I want to see only requests assigned to me so I know what to work on.
- As an **agent**, I want to mark a request "waiting for info" so the submitter knows I need more details.
- As a **manager**, I want to see every request so I can spot SLA risks.
- As a **manager**, I want to reassign a request if the current agent is out sick.

### 2.4 Requirements traceability

| Requirement | Class / method                                                   |
|-------------|------------------------------------------------------------------|
| FR1         | `AuthService.login`                                              |
| FR2, FR3    | `RequestService.createRequest` -> `Request` constructor          |
| FR4         | `RequestService.assignRequest` -> `Request.assign`               |
| FR5         | `RequestService.transitionStatus` -> `Request.transitionTo`      |
| FR6         | `RequestService.transitionStatus(CLOSED)` (authorization branch) |
| FR7         | `RequestService.addComment` -> `Request.addComment`              |
| FR8         | `RequestService.changePriority`                                  |
| FR9         | `RequestService.listFor`                                         |
| FR10        | `Request.getSlaDeadline`, `Request.isOverdue`                    |
| FR11        | `SqliteUserRepository`, `SqliteRequestRepository`                |

---

## 3. Object-Oriented Analysis & Design

### 3.1 Design philosophy

Following Session 6's "low representational gap" principle, classes mirror
the real-world domain: the HR manager of our fictional company would
recognize every class name. We avoid technical-only names like "Controller"
or "Manager" in the technical sense where a domain word fits.

### 3.2 Domain classes

**`User` — abstract** (Session 5 abstraction). Common attributes live here;
subclasses identify the role. Declared `abstract` because "a generic user"
is not a thing you ever instantiate.

**`Employee` / `ServiceAgent` / `Manager` — concrete subclasses**. Each
overrides `getRole()` to return its short code ("EMP", "AGENT", "MGR"). This
is polymorphism (Session 2) — callers work with `User` and don't need to
know which subclass.

**`Request` — central domain entity**. Owns its lifecycle invariants.
`transitionTo(Status)` checks an immutable map of allowed transitions before
mutating; illegal transitions throw `InvalidStatusTransitionException`. The
state machine is encapsulated — callers cannot bypass it by setting `status`
directly because there is no public setter.

**`Comment` — part-of a `Request`** (Session 6 composition). A comment has
no life outside its Request, which the database enforces with `ON DELETE
CASCADE`.

**`Status`, `Category`, `Priority` — enums**. Type-safe replacements for
strings. `Priority` carries its SLA hours as state (4/24/72/168), showing
that Java enums are full classes.

### 3.3 Service and persistence layers

**Services** (`AuthService`, `RequestService`) orchestrate and enforce
authorization. The authorization rules are concentrated in a single switch
statement in `RequestService.authorizeTransition` — one place to audit.

**Repositories** (`UserRepository`, `RequestRepository`) are interfaces; the
services depend only on these abstractions. `SqliteUserRepository` and
`SqliteRequestRepository` realize them with JDBC + prepared statements.
In-memory implementations realize the same interfaces for testing — direct
evidence that Dependency Inversion (Session 6) paid off.

### 3.4 UML Class Diagram

See `docs/class-diagram.svg`. Key relationships:

- Inheritance: `User` ← `Employee` / `ServiceAgent` / `Manager`.
- Composition: `Request` ◆—— `Comment` (filled diamond).
- Association: `Request` — `User` (submitter 1, assignee 0..1).
- Realization: SQLite repos ┄▷ repository interfaces.
- Dependency: services ┄▶ repository interfaces.

Methods on the diagram are filtered with Session 6's four-question test —
getters, setters, constructors, `equals`/`hashCode`/`toString` are omitted.

### 3.5 Database design

Three SQLite tables — `users`, `requests`, `comments` — with foreign keys,
`ON DELETE CASCADE` on comments (backing the composition), and indexes on
the columns used by the list queries. See `docs/er-diagram.svg`.

Datetimes are stored as ISO-8601 strings (SQLite has no native DATETIME).
Enums are stored as their `name()` string so `SELECT * FROM requests` is
human-readable.

### 3.6 Request lifecycle (state machine)

```
NEW ──assign──▶ ASSIGNED ──start──▶ IN_PROGRESS ──wait──▶ WAITING_FOR_INFO
                    │                    │    ▲                  │
                    │                    │    └──resume──────────┘
                    │                    ▼
                    │                RESOLVED ──close──▶ CLOSED
                    │                    │
                    │                    └──reopen──▶ IN_PROGRESS
                    │
                    └──unassign (manager)──▶ NEW
```

The map of allowed transitions lives in `Request.ALLOWED_TRANSITIONS` and is
exhaustively tested in `RequestTest`.

---

## 4. Applied OOP Principles

Each principle below maps to a concrete location in the codebase, so the
grader can click through and see the evidence.

**Encapsulation** (Session 2). All fields private. State changes that
affect invariants (status, assignee) go through domain methods, not setters.
*Example:* `Request.transitionTo`.

**Inheritance** (Session 2). `User` → `Employee` / `ServiceAgent` / `Manager`.
Used for a genuine is-a relationship, not mere code reuse.

**Polymorphism** (Session 2). `RequestService` handles `User` uniformly; the
subclass identity becomes relevant only inside role-based authorization,
where `instanceof` pattern-matching makes the check explicit.

**Abstraction** (Session 5). `User` is abstract. `UserRepository` and
`RequestRepository` are interfaces. Client code targets the abstraction, not
the implementation.

**Custom exceptions** (Session 5). Four domain-specific exceptions
(`InvalidStatusTransitionException`, `AuthenticationException`,
`AuthorizationException`, `ValidationException`) make the service API
self-documenting.

### SOLID (Session 6)

- **S — Single Responsibility.** Domain / service / repository / web
  separation. `Request` owns invariants; `RequestService` orchestrates;
  `SqliteRequestRepository` persists; `RequestController` handles HTTP.
- **O — Open/Closed.** Adding a new `Category` or a new repository
  implementation requires no change to existing classes.
- **L — Liskov Substitution.** Any `User` subclass works anywhere a `User`
  is expected. Any `RequestRepository` implementation (SQLite or in-memory)
  works inside `RequestService`.
- **I — Interface Segregation.** `UserRepository` and `RequestRepository`
  are small, focused interfaces rather than one big `Repository` interface.
- **D — Dependency Inversion.** `RequestService` depends on
  `RequestRepository` (abstraction), not `SqliteRequestRepository`
  (implementation). The test suite exploits this by swapping in an in-memory
  fake.

---

## 5. Design Decisions and Trade-offs

| Decision                           | Why                                               | Cost / mitigation                        |
|------------------------------------|---------------------------------------------------|------------------------------------------|
| Javalin (not Spring Boot)          | 1 dependency, < 1 s startup, beginner-friendly    | No auto-config; we wire everything by hand — intentional |
| SQLite (not PostgreSQL)            | No install, single file, ships in the JAR         | Single-writer; fine for demo             |
| Plain JDBC (not JPA/Hibernate)     | No reflection magic; every line explainable       | Hand-written row mappers                 |
| Plain-text passwords               | Scope decision for a course project               | One-line upgrade path to bcrypt in `AuthService` |
| Inline HTML via text blocks        | No template engine dependency                     | Slightly chatty controller code          |
| Single shared JDBC connection      | SQLite is single-writer anyway                    | Not suitable for multi-process deployment |
| Abstract `User` (no `Role` field)  | Textbook inheritance, clear polymorphism          | One new subclass per role                |
| Repository interfaces              | Dependency Inversion, testability                 | One extra file per entity                |

---

## 6. Testing

Three-tier strategy (see `docs/testing-strategy.md`): domain, service, and
integration. **46 test methods** across 5 test classes.

The service tests use in-memory repository fakes to isolate the layer under
test; the integration test uses a real SQLite database in a JUnit `@TempDir`
to prove the persistence mapping. HTTP / UI tests are deliberately out of
scope.

---

## 7. Project Artefacts

- **Source code:** `src/main/java/com/fs/srs/`
- **Tests:** `src/test/java/com/fs/srs/`
- **Class diagram:** `docs/class-diagram.svg`
- **ER diagram:** `docs/er-diagram.svg`
- **Testing strategy:** `docs/testing-strategy.md`
- **Code provenance:** `docs/code-provenance.md`
- **Contribution statement:** `docs/contribution-statement.md`
- **README:** `README.md` (how to build and run)

---

## 8. How to Demo

1. `mvn clean package && java -jar target/srs.jar`
2. Open `http://localhost:7070`, log in as `emp1` / `pw`, submit a request.
3. Log in as `manager1`, reassign the request to `agent2`.
4. Log in as the new assignee, progress it through IN_PROGRESS → RESOLVED.
5. Log in as `emp1` again, close the resolved request.
6. Open SQLite file with any viewer to show the real persistence.

This six-step path exercises every role, every state transition, and the
persistence layer — in under two minutes.
