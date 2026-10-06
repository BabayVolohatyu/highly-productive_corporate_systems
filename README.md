# Server monitoring

Lab foundation for a monitored-server catalog. Server fields are stored in a third-normal-form entity-attribute-value schema. The browser signs in with Keycloak (OIDC). API clients send a JWT.

## Run

JDK 27 is required. Point `JAVA_HOME` at it if `java` is not on your PATH:

```text
JAVA_HOME=C:\Users\babay\.jdks\openjdk-27
```

From the project root:

```text
docker compose up -d
mvnw.cmd spring-boot:run
```

The app reads `.env`. Copy `.env.example` if you do not have one yet. PostgreSQL is published on port 5432. Keycloak is on http://localhost:8081. The app is on http://localhost:8080.

Open http://localhost:8080 and choose **Log in with Keycloak**.

| User | Password | Role |
| --- | --- | --- |
| alice | alice | ADMIN |
| bob | bob | OPERATOR |

Keycloak admin console: http://localhost:8081 (`admin` / `admin`).

## Reset the application database

`-pg_reset` drops and recreates only the database named by `POSTGRES_DB`, then Liquibase builds the catalog again. Keycloak data is not touched.

```text
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="-pg_reset"
```

In IntelliJ, add `-pg_reset` as a program argument. The database container must already be running.

## What to show

Import `postman/server-monitoring-lab1.postman_collection.json` and run the folder from top to bottom.

- Create, list, get, update, and delete with Alice's token: CRUD over the EAV catalog. MapStruct maps the entity graph to the response.
- A clean database gets its tables from Liquibase on startup (`ddl-auto=validate`).
- The UI at `/servers` performs the same create, edit, and delete flow after the Keycloak redirect.
- A request with `Authorization: Bearer <token>` does not use the browser session.
- Bob calling `GET /api/admin/overview` returns 403 from the security filter chain.
- Bob calling `DELETE /api/servers/{id}` returns 403 from `@PreAuthorize` on `ServerService.delete`. The filter chain allows any authenticated caller on that path.
- Alice calling both of those endpoints succeeds.

## Lab 2: unit tests and coverage

Unit tests use Mockito only (no Spring context, database, or network). Run them without the Lab 1 integration test:

```text
mvnw.cmd test -Dtest=!*ApiSecurityTest
```

Open the JaCoCo HTML report after tests:

```text
target\site\jacoco\index.html
```

Enforce the 60% instruction coverage gate (includes `ApiSecurityTest` if you run the full suite):

```text
mvnw.cmd verify
```

For **AC4** at defense, compare **`lab2`** (submitted work) with **`lab2-broken`** (intentionally broken hostname uniqueness):

```text
git diff lab2..lab2-broken -- src/main/java/org/example/monitoring/server/ServerService.java
```

## Lab 3: custom annotations

Work on branch **`lab3`**. The 3NF EAV catalog is unchanged (no Liquibase or schema edits).

Three annotations, three mechanisms:

| Annotation | Type | Role |
| --- | --- | --- |
| `@ConsistentProductionStatus` | Bean Validation constraint | Class-level rule on `ServerRequest`: PROD servers cannot have status UNKNOWN |
| `@CreateServer` | Composed (meta-)annotation | Replaces `@RequestMapping(POST)` + `@ResponseStatus(CREATED)` on server create |
| `@CurrentOperator` | MVC argument resolver | Injects JWT `preferred_username` into `GET /api/admin/overview` as `requestedBy` |

Postman (same collection as Lab 1): after auth, **Lab3 PROD with UNKNOWN status is 400** proves the constraint; **AC6 admin overview allowed** also checks `requestedBy` is `alice`.

Run unit and slice tests without Testcontainers:

```text
mvnw.cmd test -Dtest=!*ApiSecurityTest
```

## Lab 4: aspect-oriented programming

Work on branch **`lab4`**. The 3NF EAV catalog is unchanged (no Liquibase or schema edits).

Three annotations, three aspects:

| Annotation | Advice | Role |
| --- | --- | --- |
| `@CountExternalCalls` | `@Before` | Counts proxied `ServerService.findById` calls in `CallMeter` |
| `@WithinBudget(maxMillis)` | `@Around` | Times `findAll`; over-budget work throws `BudgetExceededException` (503) |
| `@MaskManagementAddress` | `@Around` | Masks the last IPv4 octet on `GET /api/servers/{id}/masked` only |

Self-invocation demo: `GET /api/servers/{id}/unmetered` calls `findById` inside the same class, so the counter does not move. `GET /api/servers/{id}` hits the proxy and increments the counter. Admin overview exposes `findByIdEntries` and `lastListMillis`.

Postman: after **AC1 update server**, run the **Lab4** requests (baseline overview, proxied get, unmetered get, masked get) before the remaining AC6/AC7 steps.

Run unit and slice tests without Testcontainers:

```text
mvnw.cmd test -Dtest=!*ApiSecurityTest
```
