# Enterprise Service Request Management System (OO Analysis & Design)

Course: Systems Development (Prof. Dr. Kai Spohrer)

---

# 1. Project Overview

## 1.1 Problem statement

Many companies still manage internal service requests through emails or shared inboxes. This often leads to unclear responsibilities, missing transparency, and inefficient request handling.

The goal of this project was to develop a small web application that allows employees to submit and track requests, service agents to process them, and managers to oversee the overall workflow.

## 1.2 Scope

Included features:

* User authentication with three user roles
* Request lifecycle management from submission to closure
* Comment functionality
* SLA tracking depending on request priority
* Role-based filtered views
* Persistent data storage using SQLite

Some features were intentionally excluded to keep the project manageable within
the course scope and development timeframe. However, the architecture was
designed in a way that future extensions could be integrated without major
restructuring.

Excluded features:

* Email notifications
* File attachments
* Password hashing
* Full-text search
* Reporting exports
* External integrations

## 1.3 Actors and roles

* **Employee** — submits requests, comments on own requests, closes resolved
  requests.
* **Service Agent** — takes ownership of requests, updates lifecycle states,
  requests additional information, and resolves requests.
* **Manager** — oversees all requests, reassigns tickets, changes priorities,
  and can execute every allowed lifecycle transition.

---

# 2. Requirements Engineering

## 2.1 Functional requirements

| ID   | Requirement                                                                           |
| ---- | ------------------------------------------------------------------------------------- |
| FR1  | Users authenticate with a username and password.                                      |
| FR2  | An employee can submit a new request with title, description, category, and priority. |
| FR3  | The system assigns an ID, timestamps, and initial status `NEW`.                       |
| FR4  | A manager or agent can assign a request to a service agent.                           |
| FR5  | The assignee can move the request through its lifecycle.                              |
| FR6  | The submitter can close a resolved request.                                           |
| FR7  | Any authorized user can add comments.                                                 |
| FR8  | A manager can change priority at any time.                                            |
| FR9  | Each user sees a list filtered by role (own / assigned / all).                        |
| FR10 | An SLA deadline is computed from priority and visible on each request.                |
| FR11 | Requests are persisted across restarts.                                               |

## 2.2 Non-functional requirements

| ID   | Requirement                                                                  |
| ---- | ---------------------------------------------------------------------------- |
| NFR1 | Runs on a student laptop with a single command (`java -jar target/srs.jar`). |
| NFR2 | Under 10 MB of dependencies.                                                 |
| NFR3 | Starts in under 2 seconds.                                                   |
| NFR4 | No external services required (self-contained SQLite file).                  |
| NFR5 | Test suite runs in under 10 seconds.                                         |
| NFR6 | Code should remain understandable for students with limited Java experience. |

## 2.3 User stories (selection)

* As an **employee**, I want to submit a request so that the responsible team can process it.
* As an **employee**, I want to see only my own requests so the interface stays manageable.
* As an **agent**, I want to see requests assigned to me so I know what to work on.
* As an **agent**, I want to mark a request as "waiting for information" so the employee knows more details are needed.
* As a **manager**, I want to see all requests so I can identify SLA risks early.
* As a **manager**, I want to reassign requests if an agent is unavailable.

## 2.4 Requirements traceability

| Requirement | Class / method                                              |
| ----------- | ----------------------------------------------------------- |
| FR1         | `AuthService.login`                                         |
| FR2, FR3    | `RequestService.createRequest` -> `Request` constructor     |
| FR4         | `RequestService.assignRequest` -> `Request.assign`          |
| FR5         | `RequestService.transitionStatus` -> `Request.transitionTo` |
| FR6         | `RequestService.transitionStatus(CLOSED)`                   |
| FR7         | `RequestService.addComment` -> `Request.addComment`         |
| FR8         | `RequestService.changePriority`                             |
| FR9         | `RequestService.listFor`                                    |
| FR10        | `Request.getSlaDeadline`, `Request.isOverdue`               |
| FR11        | `SqliteUserRepository`, `SqliteRequestRepository`           |

---

# 3. Object-Oriented Analysis & Design

## 3.1 Design philosophy

The system was designed using a layered structure consisting of domain, service, repository, and web components.

During development, we focused on keeping the architecture understandable and easy to maintain while still applying the object-oriented concepts discussed during the course.

## 3.2 Domain classes

### `User` — abstract base class

`User` contains shared attributes and behavior that apply to all users.
The class is abstract because a generic user never exists directly in the
system — every user must belong to a concrete role.

The subclasses `Employee`, `ServiceAgent`, and `Manager` each override
`getRole()` individually, allowing the service layer to work with the common
`User` abstraction while still supporting role-specific behavior.

### `Employee`, `ServiceAgent`, `Manager`

The subclasses represent the three application roles. Using separate classes
instead of a simple role string made the domain model more explicit and easier
to understand.

We decided to use separate subclasses because it made the role structure easier to understand during development.

### `Request`

`Request` is the central domain entity of the application and contains the
main lifecycle logic.

The lifecycle transitions are controlled through the
`transitionTo(Status)` method. Allowed transitions are centrally defined in
`Request.ALLOWED_TRANSITIONS` to keep the lifecycle logic consistent and
easier to maintain.

Invalid transitions throw an `InvalidStatusTransitionException`.
Because the status field has no public setter, the lifecycle rules cannot be
bypassed accidentally.

One challenge during development was keeping the lifecycle logic consistent
across both the domain model and the service layer. Initially, some transition
checks were implemented directly inside the service methods, which made the
logic harder to maintain. We later centralized the lifecycle rules inside the
`Request` entity.

### `Comment`

`Comment` belongs directly to a `Request` and has no independent lifecycle.
This relationship is modeled as composition both in the UML diagram and on the
database level using `ON DELETE CASCADE`.

### `Status`, `Category`, `Priority`

Enums were used instead of plain strings to improve type safety and reduce the
risk of invalid values.

`Priority` additionally stores SLA hours directly inside the enum constants.

## 3.3 Service and persistence layers

The service layer (`AuthService`, `RequestService`) contains the main business
logic and authorization rules.

`RequestService` coordinates the interaction between users, requests,
authorization checks, and persistence operations.

Repositories (`UserRepository`, `RequestRepository`) abstract the persistence
layer from the business logic. The services depend only on repository
interfaces instead of concrete SQLite implementations.

This also made testing easier because the SQLite repositories could be replaced with in-memory versions.

We intentionally used a lightweight technology stack so that all important parts of the application remained understandable for the whole team.

## 3.4 UML Class Diagram

See `docs/class-diagram.svg`.

Key relationships:

* Inheritance: `User` ← `Employee` / `ServiceAgent` / `Manager`
* Composition: `Request` ◆—— `Comment`
* Association: `Request` — `User`
* Realization: SQLite repositories implement repository interfaces
* Dependency: services depend on repository interfaces

The UML diagram focuses mainly on the methods and relationships that are
relevant for understanding the business logic. Boilerplate methods such as
getters, setters, or `toString()` were intentionally omitted to improve
readability.

## 3.5 Database design

The persistence layer consists of three SQLite tables:

* `users`
* `requests`
* `comments`

Foreign keys are used to maintain relationships between entities.
Comments use `ON DELETE CASCADE` because comments should not exist without a
corresponding request.

Indexes were added to the columns used frequently in filtered request queries.

SQLite does not provide a dedicated DATETIME type, therefore timestamps are
stored as ISO-8601 strings.

Enums are stored using their `name()` value so the database stays easier to read during development.

See `docs/er-diagram.svg`.

## 3.6 Request lifecycle (state machine)

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

The request lifecycle was one of the central parts of the project because many
business rules depend on valid status transitions.

Allowed transitions are centrally defined in
`Request.ALLOWED_TRANSITIONS`, while the `transitionTo()` method validates
whether a requested transition is permitted.

The most important transition paths are covered through dedicated unit tests.

---

# 4. Applied OOP Principles

Several object-oriented concepts from the course were applied throughout the project.

## Encapsulation

All important fields are private.

State changes that affect business rules are handled through domain methods
instead of direct setters.

Example:

* `Request.transitionTo`
* `Request.assign`

## Inheritance

`Employee`, `ServiceAgent`, and `Manager` inherit from the abstract `User`
class.

Inheritance was used because the subclasses share common attributes but still
represent different roles inside the system.

## Polymorphism

The service layer works primarily with the abstract `User` type while the
actual subclass determines role-specific behavior.

This becomes visible during authorization checks and repository mapping.

## Abstraction

`User` is abstract because users only exist in concrete forms.

`UserRepository` and `RequestRepository` are interfaces that hide the
implementation details of the persistence layer.

## Custom exceptions

The application uses custom exceptions to make business errors easier to
understand and debug.

Implemented exceptions:

* `InvalidStatusTransitionException`
* `AuthenticationException`
* `AuthorizationException`
* `ValidationException`

## SOLID principles

### Single Responsibility Principle

Each layer has a dedicated responsibility.

### Open/Closed Principle

New features could be added without changing large parts of the existing code.

### Liskov Substitution Principle

All `User` subclasses can be used wherever a `User` is expected.

### Interface Segregation Principle

Repositories are separated into focused interfaces.

### Dependency Inversion Principle

Services depend on repository abstractions instead of concrete implementations.

---

# 5. Design Decisions and Trade-offs

| Decision                            | Why                                            | Cost / mitigation                               |
| ----------------------------------- | ---------------------------------------------- | ----------------------------------------------- |
| Javalin instead of Spring Boot      | Lightweight, fast startup, beginner-friendly   | More manual configuration                       |
| SQLite instead of PostgreSQL        | No installation required, single-file database | Not suitable for large-scale concurrent systems |
| Plain JDBC instead of JPA/Hibernate | Easier to understand and explain               | Manual row mapping                              |
| Plain-text passwords                | Reduced complexity for course scope            | Could later be replaced with password hashing   |
| Inline HTML via Java text blocks    | Avoids additional template engine dependency   | Larger controller methods                       |
| Single shared JDBC connection       | Fits SQLite's single-writer design             | Limited scalability                             |
| Abstract `User` hierarchy           | Clear domain structure and polymorphism        | One additional class per role                   |
| Repository interfaces               | Better testability and flexibility             | Slightly more boilerplate                       |

---

# 6. Testing

We separated testing into three areas:

* Domain logic
* Service logic
* Database integration

This helped us test business rules independently from the persistence layer.

The project contains unit and integration tests for the main business logic.

The service tests use in-memory repository implementations to isolate the
business logic during testing.

Integration tests use a temporary SQLite database to verify that the database
mapping behaves correctly under realistic conditions.

HTTP and frontend testing were intentionally excluded from the project scope.

---

# 7. Team collaboration

The project was developed iteratively in multiple smaller implementation
phases. Most architectural decisions were discussed together before the
implementation work was divided into separate focus areas.

To reduce merge conflicts and maintain consistency, each member mainly worked
on one technical area while still reviewing and discussing changes as a team.

In addition to the technical implementation, several project tasks were handled
collaboratively by the whole team. This included maintaining the Jira board,
organizing tasks and development progress, preparing the presentation slides,
and rehearsing the final project presentation and live demo together.

---

# 8. Contribution Statement

The project was developed collaboratively by all team members. While major
architectural and design decisions were discussed together, each member took
primary responsibility for specific areas of the system.

## Kevin: Architecture & Service Layer

Kevin focused mainly on the overall application architecture and the service
layer. This included the coordination of the layered structure
(domain → repository → service → web), the implementation of the business
logic inside `AuthService` and `RequestService`, and the integration of the
web layer using Javalin.

In addition, Kevin contributed to the overall application flow and supported
the integration between persistence and frontend functionality.

## Melvin: Domain Model, State Machine & Testing

Melvin primarily worked on the domain model and lifecycle logic of the
application. This included the implementation of the `Request` entity,
the request state machine, user abstractions, enums, and custom exceptions.

A major focus was ensuring that lifecycle transitions and domain invariants
remained consistent and testable. Melvin also contributed to the unit tests
for the domain layer.

## Patricia: OOAD Documentation, UML & Design Principles

Patricia focused on the object-oriented analysis and design documentation,
including the UML class diagram, ER diagram, and the documentation of SOLID
principles and design decisions.

Additionally, Patricia contributed to the repository abstractions and helped
align the documentation with the implemented architecture.

## Kaan: Persistence Layer & Database Integration

Kaan primarily worked on the persistence layer and SQLite integration. This
included the database schema, repository implementations, JDBC mapping,
and database-related integration tests.

Another focus area was ensuring stable end-to-end functionality during the
application demo and validating the persistence workflow.

## Shared Contributions

All team members contributed to project discussions, debugging sessions,
testing, Jira organization, presentation preparation, and slide design.
Architectural decisions and major design trade-offs were discussed
collaboratively before implementation.

---

# 9. Project Artefacts

* **Source code:** `src/main/java/com/fs/srs/`
* **Tests:** `src/test/java/com/fs/srs/`
* **Class diagram:** `docs/class-diagram.svg`
* **ER diagram:** `docs/er-diagram.svg`
* **Testing strategy:** `docs/testing-strategy.md`
* **Code provenance:** `docs/code-provenance.md`
* **Contribution statement:** `docs/contribution-statement.md`
* **README:** `README.md`

---

# 10. How to Demo

1. Run:
   `mvn clean package && java -jar target/srs.jar`

2. Open:
   `http://localhost:7070`

3. Log in as `emp1` / `pw` and submit a request.

4. Log in as `manager1` and assign the request to a service agent.

5. Log in as the assigned agent and move the request through the lifecycle.

6. Log in again as the employee and close the resolved request.

7. Optionally open the SQLite database file to demonstrate persistence.

This demo flow exercises all user roles, the request lifecycle, authorization
logic, and the persistence layer in a short end-to-end scenario.
