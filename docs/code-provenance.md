# Code Provenance Statement

*Course:* Systems Development (Prof. Dr. Kai Spohrer, Frankfurt School)
*Required by:* Session 1 group project brief
*Repository:* `c87wqs295f-del/system-development-srs-final`
*Submission date:* 18 May 2026

This document declares, as faithfully as we can, the origin of every
non-trivial piece of code and artefact in this repository. It should be read
together with `docs/contribution-statement.md`, which records *who* did what,
and `docs/ooad.md`, which records *why*. This file records *where the material
came from*.

---

## 1. Team members

| Name                      | Student ID    | Role on this project                         | GitHub                   |
|---------------------------|---------------|----------------------------------------------|--------------------------|
| Kevin Luca Queckbörner    | 8510697       | Architecture & service layer, web flow       | `kevinlqueck`            |
| Melvin Borse              | 8514759       | Domain model, state machine, tests           | `Melvin1903`             |
| Patricia G.               | 8511123       | OOAD documentation, UML & ER diagrams        | `Pati-carioca`           |
| Kaan A.                   | 8510374       | Persistence layer, schema, demo & deployment | `kaanap`                 |

All four members contributed to architectural discussions, code review,
testing, presentation rehearsal, and Jira board upkeep. See
`docs/contribution-statement.md` for the per-artefact ownership table and
the approximate hour split.

---

## 2. Tools used

| Category          | Tool                                                | Notes                                                   |
|-------------------|-----------------------------------------------------|---------------------------------------------------------|
| IDE               | Eclipse IDE for Java Developers (2024-03)           | Primary IDE for all team members; consistent formatting |
| JDK               | OpenJDK 17 (Temurin)                                | Source / target level set in `pom.xml`                  |
| Build tool        | Apache Maven 3.9.x                                  | Single-module project, fat JAR via `maven-shade-plugin` |
| Test framework    | JUnit Jupiter 5.10.2                                | Surefire 3.2.5; reports in `target/surefire-reports/`   |
| Version control   | Git + GitHub                                        | Public repository under `c87wqs295f-del`                |
| Issue tracker     | Jira (project key WT)                               | One ticket per feature/bug; linked from commit messages |
| Diagramming       | draw.io (29.6.6) for UML and ER                     | Source files: `docs/class-diagram.drawio`, `docs/erdiagram.drawio` |
| Slides            | Microsoft PowerPoint                                | `docs/presentation.pptx`                                |
| Containerization  | Docker (added for the live demo)                    | `Dockerfile` based on `maven:3.9.9-eclipse-temurin-17`  |
| Deployment        | Render (free web service tier) for the demo URL     | Reads `PORT` env var; no secrets in the repository      |


### 2.1 AI-supported tools

Consistent with `docs/contribution-statement.md` §5, AI-supported tools
(OpenAI ChatGPT and Anthropic Claude, via their respective web interfaces)
were used during the project as supporting development tools. They were
**not** used to design the system, to choose the architecture, or to author
the business logic.

Concretely, AI tools were used in the following situations:

- **Frontend / HTML / CSS scaffolding.** Web/UI work was not covered in
  depth during the course, so AI was used to suggest initial markup
  structure for the Javalin text-block templates (e.g. the table layout in
  `RequestController.list`, the form structure in `showNewForm`/`showLogin`)
  and to suggest CSS variable naming and the colour palette in
  `style.css`. All output was reviewed, simplified, and adapted by the team
  before commit.
- **Documentation polishing.** Rough team-written bullet points for the
  OOAD document, the testing-strategy note, and ticket descriptions were
  occasionally passed through an AI tool to tighten prose and check
  consistency of terminology. Technical content (requirements, design
  decisions, trade-offs) originates from the team.
- **Debugging and framework Q&A.** Short, targeted questions about Javalin
  6 routing, SQLite JDBC parameter binding, and JUnit 5 `@TempDir` usage —
  the kind of question normally answered by reading the framework docs
  faster.
- **Repetitive boilerplate.** First drafts of the `rowToUser` /
  `rowToRequest` JDBC mappers and the symmetric `bindCommon` parameter
  binder in `SqliteRequestRepository` were generated as scaffolding and then
  edited by the team to match our own naming, error handling, and the rest
  of the codebase.
- **Demo brand mark.** The `WinTick` logo image at
  `src/main/resources/static/logo.png` was generated with an AI image tool
  from team-written prompts and used as a placeholder demo brand. It does
  not represent a real product or organisation and is included only so the
  application has a recognisable header during the demo.

All AI suggestions were treated as drafts, not as authoritative output.
Final responsibility for every committed line rests with the team. Where an
AI suggestion was wrong (it occasionally proposed JPA-style annotations,
Lombok, or Javalin 4 syntax), it was discarded. The architecture, the
domain model, the lifecycle state machine, the authorization matrix, the
test design, and the SQLite schema were designed and implemented by the
team without AI authorship.

No AI tool, dataset, or model output is shipped inside the repository.

---

## 3. External dependencies

All runtime and test dependencies are declared in `pom.xml` and resolved
from Maven Central. None are vendored into the repository.

| Dependency                       | Version    | License                                  | Purpose                                  |
|----------------------------------|------------|------------------------------------------|------------------------------------------|
| `io.javalin:javalin`             | 6.4.0      | Apache License 2.0                       | Embedded web server (Jetty) and routing  |
| `org.xerial:sqlite-jdbc`         | 3.45.3.0   | Apache License 2.0                       | SQLite JDBC driver, bundles native lib   |
| `org.slf4j:slf4j-simple`         | 2.0.13     | MIT                                      | SLF4J binding used by Javalin / Jetty    |
| `org.junit.jupiter:junit-jupiter`| 5.10.2     | Eclipse Public License 2.0               | Unit & integration tests (test scope)    |

Maven plugins used during the build (`maven-compiler-plugin` 3.13.0,
`maven-surefire-plugin` 3.2.5, `maven-shade-plugin` 3.5.3) are all
Apache 2.0-licensed and standard parts of the Maven distribution.

Transitive dependencies (Jetty, Jackson, the Kotlin stdlib pulled in by
Javalin, etc.) are pulled in by the four direct dependencies above and
inherit their respective open-source licenses. No commercial or
GPL-licensed dependency is used.

The Eclipse Temurin 17 base image used in the `Dockerfile` is distributed
under the GPLv2 with Classpath Exception, consistent with the OpenJDK
licensing terms.

---

## 4. Code originality

All Java source files in `src/main/java/com/fs/srs/` and all test files in
`src/test/java/com/fs/srs/` were written by the team. No code was copied
verbatim from Stack Overflow answers, blog posts, public GitHub
repositories, other student projects, or course material that was not
ours.

The team is the sole author of:

- The package layout (`domain` / `exceptions` / `repository` / `service` /
  `web`).
- The `Request` lifecycle state machine and the central
  `ALLOWED_TRANSITIONS` map.
- The role-based authorization rules in `RequestService`
  (`authorizeTransition`, the role checks in `assignRequest`,
  `addComment`, `changePriority`, `listFor`, `getVisible`).
- The repository interfaces (`UserRepository`, `RequestRepository`) and
  their two implementations (SQLite production, in-memory test fakes).
- The SQLite schema (`src/main/resources/schema.sql`) including index
  choices and the `ON DELETE CASCADE` on `comments`.
- The three-tier testing strategy (domain, service, integration) and the
  46 test methods that implement it.
- The hand-rolled HTML escaper in `ViewHelpers.esc` and all the inline
  HTML text-block templates in the controllers (with the scaffolding
  caveat noted in §2.1).

Where AI was used as a scaffolding aid, primarily the JDBC row mappers
and parts of the HTML/CSS. The resulting code was reviewed, edited and
re-styled by the team before commit, so that no committed file is a raw
AI artefact. The Git history (56 commits over 13–18 May 2026) shows the
incremental, file-by-file refinement work that this entailed.

---

## 5. Third-party assets

| Asset                                           | Origin                                                                                            | License / status                          |
|-------------------------------------------------|---------------------------------------------------------------------------------------------------|-------------------------------------------|
| `src/main/resources/static/style.css`           | Written by the team (primary author: Kaan A., reviewed by Patricia G. — see contribution table). | Team-owned; no external CSS frameworks    |
| `src/main/resources/static/logo.png` (WinTick)  | Generated with an AI image tool (see §2.1) from team-written prompts and used as the demo brand mark for this project. "WinTick" is an invented name with no intended reference to any real organisation. | Team-owned for the purposes of this coursework |
| Fonts                                           | System fonts only (`Nunito`, `Segoe UI`, `system-ui`, `Roboto`).                                  | Loaded from the user's OS — no font files shipped |
| Favicon                                         | None shipped.                                                                                     | n/a                                       |
| UML / ER diagrams                               | Authored by the team in draw.io.                                                                  | Team-owned                                |
| Presentation deck (`docs/presentation.pptx`)    | Authored by the team in PowerPoint.                                                               | Team-owned                                |

No images, icons, audio or other media from third-party catalogues are
included in this repository.

---

## 6. Data sources

The demo dataset created by `Main.Seed` on first run is entirely
fictional. The five seeded user accounts (`manager1`, `agent1`, `agent2`,
`emp1`, `emp2`), their full names ("Maria Boss", "Adam Agent", "Ava
Agent", "Erik Employee", "Elena Employee"), the example email addresses
(`@example.com`), and the three demo request titles ("Laptop won't boot",
"Office AC not working", "New hire onboarding for Tom") were written by
the team for illustration purposes only.

No real employee, customer, or company data appears in this repository or
in the seeded SQLite database file `data/srs.db`.

The demo passwords are deliberately the literal string `"pw"`, which is a
documented Phase-1 scope decision (see `docs/ooad.md` §5 and
`docs/testing-strategy.md`); they are not credentials reused from any
real system.

---

## 7. Copyright

We confirm that:

1. We hold the copyright on the original code, documentation, diagrams,
   stylesheet, and logo we have authored.
2. All third-party software we depend on is open-source under the
   licenses listed in §3, and our use complies with each license's
   terms (attribution preserved in the Maven dependency metadata; no
   redistribution of modified third-party source).
3. We have not included any copyrighted material, text, images, code,
   trademarks, or data, for which we do not hold a license or fair-use
   right.
4. The "WinTick" name and logo are an invented brand created for this
   academic exercise and do not refer to any real product or
   organisation.

---

## 8. Declaration

We confirm that the submitted code is our own work, that external sources
are properly attributed above, that any use of AI-supported tools is
disclosed in §2.1 and in `docs/contribution-statement.md` §5, and that we
understand and accept the Frankfurt School rules on academic integrity.

Date: 18 May 2026

Signatures (typed names acceptable for digital submission):

Kevin Luca Queckbörner

Melvin Borse

Patricia G.

Kaan A.
