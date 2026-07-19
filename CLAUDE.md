# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository shape

This is `API_authentification`, the Spring Boot identity service for the Gatcha game microservices. It is normally checked out as a git submodule of the root orchestration repo (`GatchaApi`), which wires it up alongside `API_joueur`, `API_monstres`, `API_invocations`, `API_generate_gatcha`, and `Gatcha_Front` via a root `docker-compose.yaml`/`Makefile`. This directory also has its own standalone `docker-compose.yml` for running the service in isolation — **do not run both the root and local compose stacks at once**, they share container names/ports (`mongo`, `8080`) and will conflict.

The service is intentionally minimal: one controller, a couple of services, one Mongo-backed repository, and a hand-rolled AES token scheme — now carrying a `USER`/`ADMIN` role. Per the README, it is **not production-hardened** — do not treat any part of this as a security-reviewed reference implementation without scrutiny.

## Commands

From within this directory (Maven wrapper, no local Maven install needed):

```bash
./mvnw clean package                            # build
./mvnw test                                     # full test suite
./mvnw test -Dtest=UserControllerTest           # single test class
./mvnw test -Dtest=UserControllerTest#login_shouldReturnOk_whenLoginIsSuccessful  # single test method
```

Local Docker (standalone, self-contained — starts Mongo + this service):

```bash
make up          # docker compose up -d --build
make down         / make down-v   # down-v also wipes the mongo volume
make ps / make logs / make build
```

As a submodule of the root repo (targets the root `docker-compose.yaml`, only this service):

```bash
make global-up / global-down / global-down-v / global-reset-volumes / global-ps / global-logs / global-restart
```

`make help` lists all targets. There is no lint/format tooling configured for this service (unlike `API_invocations` in the sibling repo, which has spotless/checkstyle).

Requires a `.env` file (see `.env.example`) with `AUTH_SECRET` and `AUTH_SALT` — the app fails fast at startup (`AuthHandler` constructor throws `IllegalStateException`) if either is missing/blank. Optionally also set `DEFAULT_ADMIN_USERNAME`/`DEFAULT_ADMIN_PASSWORD` and `DEFAULT_USER_USERNAME`/`DEFAULT_USER_PASSWORD` to seed one default admin and one default user account on startup (`config/DefaultUsersSeeder.java`, a `CommandLineRunner`) — unlike `AUTH_SECRET`/`AUTH_SALT` these are optional and idempotent: blank or already-existing accounts are silently skipped, nothing fails if they're unset.

Swagger UI: `http://localhost:8080/swagger-ui/index.html` (port 8081 on the host when run via the root stack, since the root compose remaps it).

## Architecture

**This service is the identity source of truth for the whole platform, but it does not issue JWTs.** Tokens are an opaque, symmetrically-encrypted blob — not a standard/inspectable format — and every other service must call back into this API to validate one.

Flow, end to end:
- `UserController` (`controller/UserController.java`) exposes `POST /user` (register, always creates a `USER`), `POST /user/login`, `POST /user/verify-token`, `POST /user/delete` (self-delete, any authenticated user), `POST /user/admin/register` (admin-only, creates an account with an arbitrary `Role`), and `POST /user/admin/delete/{username}` (admin-only, deletes any account). All of it is unauthenticated at the transport level — there is no Spring Security filter chain; auth/authorization is purely "does this opaque token decrypt to a still-valid `Token`, and does its role satisfy the endpoint's requirement," checked via `service/AuthorizationService.java` (`requireValidToken`/`requireAdmin`).
- `AuthHandler` (`utils/AuthHandler.java`) is where tokens are minted and checked. `generateToken(username, role)` serializes a `Token` (username + role + expiration, 1 hour TTL) to JSON via a dedicated, private Jackson `ObjectMapper` (`utils/Token.java` — property-name-based `@JsonCreator`/`@JsonProperty` binding, independent of field order; no longer Lombok `toString()`/regex-based) then AES/GCM-encrypts it (`utils/AESUtil.java`) with a key derived via PBKDF2 from `AUTH_SECRET`+`AUTH_SALT` (`utils/SecurityProperties.java`, bound from `app.security.secret`/`app.security.salt`). `validateToken` reverses this and returns an `AuthenticatedUser(username, role)` (`utils/AuthenticatedUser.java`) only if `expirationDate` is still in the future, else `null`. This internal JSON mapper is intentionally separate from any HTTP-layer/Spring-managed `ObjectMapper` bean.
- Passwords are hashed with BCrypt (`org.springframework.security.crypto.password.PasswordEncoder`/`BCryptPasswordEncoder`, wired via `config/PasswordEncoderConfig.java`; only the leaf `spring-security-crypto` artifact is on the classpath, not the full `spring-boot-starter-security` — no filter chain, no auto-configuration). `UserService.register` hashes on write, `UserService.checkPassword` compares via `passwordEncoder.matches` on login.
- Every account has a `Role` (`persistence/dto/Role.java`: `USER`/`ADMIN`). The public `POST /user` endpoint's request DTO (`UserHttpDTO`) has no `role` field at all, so a client can never self-elevate through it — only `POST /user/admin/register` can create an `ADMIN`, and only an existing `ADMIN` token can call it.
- Persistence is a single collection: `UserMongoDTO` (`persistence/dto`, `@Document("users")`, UUID `@MongoId`, `username` has a Mongo unique index via `@Indexed(unique = true)` + `spring.data.mongodb.auto-index-creation: true`) via `UserMongoDAO` (`persistence/dao`, a plain `MongoRepository` with one derived query, `findByUsername`).
- `config/DefaultUsersSeeder.java` (a `CommandLineRunner`) seeds one default admin and one default user on startup from `DEFAULT_ADMIN_*`/`DEFAULT_USER_*` env vars (see Commands section) — idempotent (skips if blank or already present).
- Errors funnel through `GlobalExceptionHandler` (`exception/`), a `@ControllerAdvice` mapping specific exception types (`UserDuplicateException` → 409 payload, `UserCredsException` → 401 payload, `TokenInvalidException` → 498 payload, `ValidationException` → 400 payload, `InsufficientRoleException` → 403 payload) to a uniform `Errors`/`CustomError` JSON body. **Only `InsufficientRoleException` gets an actual matching HTTP 403** — every other handler here still calls `ResponseEntity.badRequest()` regardless of the semantic code carried inside the JSON body (a pre-existing inconsistency, left as-is beyond the new 403 case); check `GlobalExceptionHandler` before assuming a handler's numeric code matches the actual response status.

**Other services depend on `POST /user/verify-token` being fast and correct** — `API_joueur` and `API_monstres` call it synchronously on every incoming request via their own `AuthInterceptor`s, and `API_invocations` forwards the original caller's token here as well. `TokenHttpResponseDTO` now also carries `role` alongside `username`; the response DTOs in those three sibling services (`API_joueur`'s and `API_monstres`' `TokenResponse`, `API_invocations`' `AuthTokenResponse`) were updated to tolerate this extra field (`@JsonIgnoreProperties(ignoreUnknown = true)` + an unused `role` property), but none of them act on the role yet — no admin-gated logic exists in those services. A change to the token format, expiration semantics, or response shape of `verify-token`/`TokenHttpResponseDTO` is a cross-service breaking change, not local to this repo.

Config: `application.yml` holds local defaults (localhost Mongo, `app.security.*` from env); `application-docker.yml` overrides just the Mongo host to `mongo` and is activated via `-Dspring.profiles.active=docker` in the `Dockerfile`'s `ENTRYPOINT`.

## Testing conventions

- `UserControllerTest` (`@WebMvcTest`) mocks `UserService`/`AuthHandler`/`AuthorizationService` and tests controller behavior/status codes in isolation, including the admin-gated endpoints (403 for non-admins).
- `UserIntegrationTest` (`@SpringBootTest` + `@AutoConfigureMockMvc`) exercises the register → login → verify-token flow through real Spring wiring (including real BCrypt hashing), but still mocks `UserMongoDAO` (Mongo auto-configuration is explicitly excluded) — there is no real database in the test suite, so no running Mongo instance is required to run tests. Because `PasswordEncoder` is real here, the login step reuses the actual `UserMongoDTO` captured from the register step's `save()` call (already BCrypt-hashed) rather than constructing a fresh one with the raw password.
- `TokenTest`/`AuthHandlerTest` (`utils/`) cover the JSON-based token round-trip (order-independent parsing, default expiration) and the full generate/validate/encrypt cycle directly.
- `DefaultUsersSeederTest` (`config/`) covers the seeding/idempotency logic in isolation (no Spring context).
