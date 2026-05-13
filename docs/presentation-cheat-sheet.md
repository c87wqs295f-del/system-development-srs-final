# Presentation Cheat Sheet

Exact sentences to say at each slide and in the live demo. Use the words or
adapt them to your voice — the important thing is that nobody freezes.

Time budget: roughly **8 minutes presentation + 2 minutes demo + Q&A**.

---

## Slide 1 — Title

> "We built an Enterprise Service Request Management System. It lets
> employees submit internal tickets, service agents handle them, and
> managers oversee the whole queue. Everything you will see today is our
> own work, written in Java 17 with Javalin and SQLite."

## Slide 2 — The problem

> "In a medium-sized company, internal service requests — IT, facilities,
> HR, supply, access — are usually sent by email. Emails get lost, there
> is no status, no SLA, no oversight. Our system replaces that with one
> shared queue that all three roles use."

## Slide 3 — Actors and flow

> "Three actors: an employee submits a request, an agent resolves it, a
> manager oversees. A request moves through a defined lifecycle, so at any
> moment everyone knows where it stands."

## Slide 4 — UML class diagram

> "This is the heart of our design. `User` is abstract — we never
> instantiate a generic user, we always have one of the three subclasses.
> That is the textbook case for `abstract` from Session 5."
>
> "`Request` is our central entity. It owns its own lifecycle through the
> `transitionTo` method — callers can only request a state change; the
> rules live inside the class. That is encapsulation."
>
> "Note the filled diamond between `Request` and `Comment`: composition.
> A comment without its request has no meaning, so the database enforces
> this with an `ON DELETE CASCADE`."
>
> "Finally, the repositories are interfaces. Services depend on the
> interface, not on SQLite. That is Dependency Inversion — principle D
> of SOLID."

## Slide 5 — State machine

> "Six states, a handful of allowed transitions. The rules are in a single
> map inside `Request`, and they are exhaustively tested — we cannot close
> a request that was never opened, we cannot resolve a request that was
> never assigned. The tests read as an executable specification."

## Slide 6 — Database (ER diagram)

> "Three tables. `users` has a discriminator column — `role` — that tells
> the repository which subclass to rebuild. `requests` has two foreign
> keys to `users`: the submitter and the optional assignee. `comments`
> are deleted with their request by foreign-key cascade, which is the
> database-level expression of the composition from the class diagram."

## Slide 7 — SOLID in practice

> "S — each package does one thing. O — adding a new category or a new
> repository implementation does not touch existing code. L — every
> `User` subclass is substitutable. I — we have two small repository
> interfaces, not one big one. D — our tests are the proof: we swap the
> SQLite repository for an in-memory fake and the services do not care."

## Slide 8 — Testing strategy

> "Three tiers. Domain tests are pure JUnit. Service tests use in-memory
> repositories — that is Dependency Inversion paying off. One integration
> test boots a real SQLite database in a temp folder and proves the
> round-trip. Forty-six tests in total, running in under three seconds."

## Slide 9 — What we deliberately left out

> "Plain-text passwords, no email notifications, no file attachments.
> Each exclusion is a scope decision, not missing work. The upgrade
> path is trivial: authentication is isolated in `AuthService`, so
> swapping to bcrypt is a one-line change. We thought it was more
> important to get the OO design right."

## Slide 10 — Live demo (2 minutes)

Demo script (say what you are doing as you do it):

1. **Log in as `emp1`.** "This is Erik Employee. He sees only his own
    requests — role-based filtering is at the service layer."
2. **Submit a new request.** "Title, category, priority — a fresh request
    starts in `NEW`."
3. **Log out, log in as `manager1`.** "Maria the manager sees every request
    in the system. Watch me reassign a ticket."
4. **Reassign to `agent2`.**
5. **Log in as `agent2`.** "Ava sees only her assignments. She takes it to
    `IN_PROGRESS`, then `RESOLVED`."
6. **Log back in as `emp1`.** "Erik gets the resolved ticket and closes it.
    That is the full lifecycle."

## Slide 11 — Technology summary

> "Four runtime dependencies, under 10 MB. One command to build, one
> command to run. Zero external services — the whole database is one
> file on disk. Anyone in this room can clone our repository and have it
> running in under a minute."

## Slide 12 — Q&A

Common questions and short answers:

- **"Why Javalin instead of Spring Boot?"** — "Scope. Spring Boot hides
  things we wanted to understand. Javalin is one jar and boots in under
  a second."
- **"Why JDBC instead of JPA?"** — "We wanted every line explainable. JPA
  adds reflection magic. Our Repository interfaces give us the swap-ability
  JPA would sell, without the complexity."
- **"Plain-text passwords?"** — "Deliberate scope decision, documented in
  the README. One-line upgrade to bcrypt in `AuthService` when we want it."
- **"What if two agents edit a request at the same time?"** — "SQLite
  serializes writes, and for a demo with one user at a time it is safe.
  In production we would add optimistic locking with a version column."
- **"How would you scale this?"** — "Two moves: swap SQLite for
  PostgreSQL by writing a new repository implementation — zero changes
  elsewhere — and run multiple Javalin instances behind a load balancer
  with sticky sessions."
- **"Where is inheritance in your code?"** — "`User` is abstract, three
  subclasses. Pull up `Employee.java`." *(have Eclipse open to it)*
- **"Where is polymorphism?"** — "`RequestService.listFor(User actor)`.
  The argument is the abstract type; each subclass dispatches to a
  different query."

---

## Body-language / delivery tips

- Stand, don't sit. Turn the laptop so the panel sees the screen.
- When a question comes up about code, open the file in Eclipse and scroll
  to it. Showing beats telling.
- If you don't know an answer, say "Good question, let me think" — never
  invent. The course values integrity over bluffing.
- Every teammate should speak at least once during the presentation.
- Run the demo once, exactly as scripted, before the presentation starts.
  The first live run is always the shakiest.
