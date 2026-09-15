# Spring Backend Kit

A production-oriented Java 21 / Spring Boot modular-monolith foundation for REST APIs. It provides a secure authentication lifecycle, user management, persistence migrations, Redis-backed rate limiting, API documentation, observability, Docker Compose, and an automated test baseline.

## Included

| Area | Implementation |
|---|---|
| Runtime | Java 21, Spring Boot 3.5, Maven |
| Persistence | PostgreSQL, Spring Data JPA, Flyway, `ddl-auto=validate` |
| Security | BCrypt, short-lived JWT access tokens, hashed rotating refresh tokens, RBAC |
| Account lifecycle | Registration, email verification, password reset, logout, account lockout |
| Platform | Redis rate limiting, Actuator health, Micrometer Prometheus metrics, request IDs |
| Quality | JUnit 5, Mockito-ready Spring test stack, Testcontainers dependencies, GitHub Actions |
| Documentation | OpenAPI/Swagger UI, architecture decisions, versioned REST endpoints |

## Quick start

Copy `.env.example` to `.env`, replace `JWT_SECRET` with a random value of at least 32 characters, then run:

```bash
cp .env.example .env
docker compose up --build
```

The API is available at `http://localhost:8080`. Swagger UI is at `http://localhost:8080/swagger-ui.html`, OpenAPI JSON is at `http://localhost:8080/v3/api-docs`, health is at `http://localhost:8080/actuator/health`, and Prometheus metrics are at `http://localhost:8080/actuator/prometheus`.

## Authentication flow

Register with `POST /api/v1/auth/register`. In the local profile, the `ConsoleEmailService` writes verification and reset tokens to application logs. Verify with `POST /api/v1/auth/verify-email`, log in with `POST /api/v1/auth/login`, and send the returned access token as `Authorization: Bearer <token>`. Refresh tokens rotate on `POST /api/v1/auth/refresh`; logout revokes the presented refresh token.

Sensitive flows are intentionally generic. `forgot-password` and resend-verification do not disclose whether an email address exists. Refresh-token plaintext is never persisted.

## API examples

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"john@example.com","password":"StrongPassword123!"}'

curl http://localhost:8080/api/v1/users/me \
  -H 'Authorization: Bearer <access-token>'
```

All endpoints use `/api/v1` versioning. Validation and domain failures use one JSON error envelope containing status, code, message, path, timestamp, trace ID, and field errors where applicable.

## Development

The Maven wrapper is included. Use `./mvnw test` on Unix or `mvnw.cmd test` on Windows. Unit tests run without external services. Integration tests should use Testcontainers with PostgreSQL in CI or a Docker-enabled development environment.

## Configuration

Database, Redis, JWT, CORS, and access-token lifetime are environment-configurable. Never commit `.env`. For production, use a secret manager, HTTPS, a trusted CORS allow-list, a real email provider implementation, and a fail-closed rate-limit policy if that is appropriate for the threat model.

## Design decisions and future work

PostgreSQL provides transactional relational consistency; Flyway makes schema evolution explicit; Redis supports distributed rate limiting; and a modular monolith preserves boundaries without premature service decomposition. Future extensions may add a real SMTP provider, permission-level authorization, session/device management, OpenTelemetry, static analysis, and full Testcontainers flow tests.

See [architecture.md](architecture.md) for the security and deployment boundary decisions.
