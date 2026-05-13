# Code Provenance Statement

*Course:* Systems Development (Prof. Dr. Kai Spohrer, Frankfurt School)
*Required by:* Session 1 group project brief

This document declares, as faithfully as we can, the origin of every
non-trivial piece of code and artefact in this repository. Fill in names,
dates and tool versions before submission.

## 1. Team members

| Name            | Student ID | Role           |
|-----------------|------------|----------------|
| _TO FILL IN_    | _TO FILL IN_ | _TO FILL IN_ |
| _TO FILL IN_    | _TO FILL IN_ | _TO FILL IN_ |
| _TO FILL IN_    | _TO FILL IN_ | _TO FILL IN_ |

## 2. Tools used

- **IDE:** Eclipse IDE for Java Developers (version _TO FILL IN_)
- **Build tool:** Apache Maven 3.x
- **Version control:** Git + GitHub
- **Issue tracker:** Jira (project key _TO FILL IN_)
- **AI assistance:** _TO FILL IN — e.g. "ChatGPT / Claude used for
  brainstorming design alternatives and reviewing code style"_

## 3. External dependencies (declared in `pom.xml`)

| Dependency        | Version   | License      | Purpose                          |
|-------------------|-----------|--------------|----------------------------------|
| Javalin           | 6.4.0     | Apache 2.0   | Embedded web server & routing    |
| sqlite-jdbc       | 3.45.3.0  | Apache 2.0   | SQLite JDBC driver               |
| slf4j-simple      | 2.0.13    | MIT          | Logging binding                  |
| JUnit Jupiter     | 5.10.2    | EPL 2.0      | Testing framework (test scope)   |

All dependencies are retrieved from Maven Central; no artefacts are vendored
into the repository.

## 4. Material learned from course slides

The design and terminology explicitly follow the course lectures. The
specific sessions referenced are:

- **Session 1** — project brief, functional requirements, lifecycle.
- **Session 2** — Java classes, inheritance, polymorphism, overriding.
- **Session 5** — abstract classes, custom exceptions.
- **Session 6** — UML notation (visibility, cardinality, association types),
  SOLID principles, "low representational gap".

Where a design decision is directly traceable to a slide, we cite the
session in the OOAD document.

## 5. Code originality

All Java source files in `src/main/java/com/fs/srs/` and test files in
`src/test/java/com/fs/srs/` were written by the team. No code was copied
from Stack Overflow, other student projects, or blog posts.

AI-assisted generation was used for: _TO FILL IN — e.g. "initial scaffolding
of the Javalin route bindings and the boilerplate `rowToUser` / `rowToRequest`
JDBC mappers; all output was reviewed and modified by the team before
inclusion."_

## 6. Third-party assets

- CSS in `src/main/resources/static/style.css` — written by the team,
  no external stylesheets or frameworks.
- Favicon — _TO FILL IN if added later_.
- Fonts — default system fonts; no custom font files shipped.

## 7. Data sources

The demo dataset in `Main.Seed` is entirely fictional. No real employee,
customer or company data appears in this repository or the seeded database.

## 8. Declaration

We confirm that the submitted code is our own work, that external sources
are properly attributed above, and that we understand the Frankfurt School
rules on academic integrity.

Date: _TO FILL IN_

Signatures (typed names acceptable for digital submission):

_TO FILL IN_
_TO FILL IN_
_TO FILL IN_
