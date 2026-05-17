# Contribution Statement

Course: Systems Development (Prof. Dr. Kai Spohrer)

This document records who contributed what, so that individual grades can
be fair and the interview can start from a shared picture of the work.

Fill in the cells with a short phrase (e.g. "primary author", "reviewed",
"pair-programmed with X"). Git history and Jira ticket ownership back up
each claim.

## 1. Team members and rough effort split

| Name         | Approx. hours | Main areas of responsibility                     |
|--------------|---------------|--------------------------------------------------|
| Kevin Q. | ~28h   | architecture, service layer, authentication, web flow |
|  Melvin B. | ~30h   | domain model, state machine, validation, tests |
|  Patricia G. | ~26h     | OOAD documentation, UML, ER diagram, SOLID analysis |
|   Kaan A.  | ~26h     | persistence layer, SQLite schema, integration tests, demo |

## 2. Per-artefact ownership

| Artefact / module                    | Primary      | Reviewer(s)  |
|--------------------------------------|--------------|--------------|
| Requirements & OOAD document          | Patricia G.  | Kevin Q., Melvin B. |
| UML class diagram                     | Patricia G.  | Melvin B. |
| ER diagram                            | Patricia G.  | Kaan A. |
| Domain classes (`domain/`)            | Melvin B. | Kevin Q.  |
| Exceptions (`exceptions/`)            | Melvin B. | Kevin Q.  |
| Repositories (`repository/`)          | Kaan A. | Kevin Q.  |
| Services (`service/`)                 | Kevin Q. | Melvin B. |
| Web layer (`web/`)                    | Kevin Q. | Kaan A.  |
| Domain tests                          | Melvin B. | Kevin Q. |
| Service tests                         | Kevin Q. | Melvin B.  |
| Integration test                      | Kaan A. | Melvin B.  |
| Stylesheet & demo data                | Kaan A. | Patricia G.   |
| Presentation deck                     | Patricia G.  | all team members |
| Rehearsal / demo script               | Kevin Q. | all team members |

## 3. Joint decisions

These were decided together in team meetings and belong to the whole group:

- Scope and phase boundaries
- Technology stack choices (Javalin, SQLite, JDBC, plain-text passwords)
- Lifecycle states and transition rules
- Authorization matrix
- UML notation conventions
- Testing strategy (three tiers)
- Demo script and interview talking points

## 4. Evidence in the repository

- **Git commits** on the GitHub repository. Each feature branch is authored
  by the primary contributor above; pull-request reviews by the stated
  reviewers.
- **Jira tickets** Every ticket is assigned to the
  person who implemented it; review comments are part of the ticket history.

## 5. Use of AI-supported tools

AI-supported tools such as ChatGPT and Claude were used selectively during the
project as supporting development tools.

The overall architecture, class design, domain model, lifecycle logic, Java
implementation, Jira organization, and UML/ER diagrams were developed by the
team independently.

Jira tickets, task structure, and ticket titles were created by the team.
ChatGPT was occasionally used to refine ticket descriptions and acceptance
criteria based on team-written ideas and implementation goals.

AI support was mainly used in areas that were not covered in detail during the
course, especially frontend implementation and UI-related code. In addition,
AI tools were used to refine documentation text based on team-written bullet
points, support debugging, explain framework behavior, and speed up repetitive
implementation tasks.

All generated suggestions were reviewed, adapted, and integrated manually by
the team. Final technical decisions, application structure, business rules,
and testing logic remained the responsibility of the project members.

## 6. Declaration

We confirm that the split above accurately reflects our individual
contributions to this project.

Date: 17.05

Kevin Q.  Melvin B.   Patricia G.   Kaan A.
