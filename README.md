# Enterprise Service Request Management System

Group project for *Systems Development* (Prof. Dr. Kai Spohrer, Frankfurt School).

A small Java / Javalin / SQLite web app that lets employees submit internal
service requests and lets agents and managers drive them through a lifecycle.

## Requirements

- Java 17 or newer
- Maven 3.6+ (or the Maven wrapper that Eclipse provides)

## Run it

```bash
mvn clean package
java -jar target/srs.jar
```

Then open <http://localhost:7070> in your browser.

On first run the app creates `data/srs.db` and seeds a demo dataset.

### Demo accounts (passwords are literally `pw`)

| Username   | Role           |
|------------|----------------|
| `manager1` | Manager        |
| `agent1`   | Service Agent  |
| `agent2`   | Service Agent  |
| `emp1`     | Employee       |
| `emp2`     | Employee       |

## Run the tests

```bash
mvn test                                       # run all tests
mvn -Dtest=RequestTest test                    # run one test class
mvn -Dtest='*ServiceTest' test                 # run all service tests
```

The suite has three tiers:

- **Domain tests** (`src/test/java/com/fs/srs/domain/`) — pure JUnit, no
  database, no services. Proves the state machine in `Request.transitionTo`
  and the SLA hours in `Priority`.
- **Service tests** (`src/test/java/com/fs/srs/service/`) — uses an in-memory
  repository fake. Proves every role × action authorization rule.
- **Integration test** (`src/test/java/com/fs/srs/repository/`) — boots a
  real SQLite database in a `@TempDir`. Proves the round-trip mapping.

## Import into Eclipse

`File → Import → Existing Maven Projects → select this folder`.
Eclipse reads `pom.xml` and configures the classpath automatically.

## Project layout

```
src/main/java/com/fs/srs/
  domain/        pure domain classes + enums
  exceptions/    custom domain exceptions
  repository/    storage abstraction + SQLite implementations
  service/       application services (auth, request orchestration)
  web/           Javalin bootstrap, controllers, HTML helpers
  Main.java      entry point — wires the layers together
src/main/resources/
  schema.sql     SQLite DDL (applied on first run)
  static/        CSS assets served under /static
src/test/java/   JUnit 5 tests
docs/            OOAD document, class diagram, provenance statement
```

## Technology choices

- **Javalin** — embedded Jetty; one dependency, starts in < 1 s.
- **SQLite via JDBC** — zero install; `sqlite-jdbc` bundles the native binary.
- **Plain-text passwords** — *deliberate* scope decision for a course project.
  See the OOAD doc for rationale and the production upgrade path.
- **No ORM** — JDBC + custom repositories are easy to read and explain.

## Changing the port

```bash
java -Dport=9090 -jar target/srs.jar
```
