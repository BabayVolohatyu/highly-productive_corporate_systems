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
