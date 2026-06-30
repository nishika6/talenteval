# Coding Conventions — TalentEval

These are the patterns actually used throughout the codebase. Follow them when adding new features so the project stays consistent.

## Backend (Spring Boot)

### Package structure

Every feature follows the same layering, organized by type (not by feature):

```
com.talenteval.talenteval
├── controller/   REST controllers — one per feature (e.g. QuestionController)
├── dto/          Request/response objects — never expose entities directly
├── entity/       JPA entities + enums
├── exception/    GlobalExceptionHandler (one handler for the whole app)
├── repository/   Spring Data JPA interfaces
├── security/     JWT utility, filter, UserDetailsService, SecurityConfig
├── service/      Business logic — one per feature
└── config/       Startup-time config, e.g. QuestionSeeder
```

### Build order per feature

Backend fully done (entity → repository → service → controller) **then** the frontend page, working end-to-end, before moving to the next feature. Don't build two features in parallel.

### DTOs, always

Controllers and services never accept or return entities directly. Every endpoint has a dedicated `*Request` DTO (with Jakarta Validation annotations) and a `*Response` DTO. Entities stay internal to the service/repository layer.

```java
// Request DTOs use @Data + validation annotations
@Data
public class QuestionRequest {
    @NotBlank(message = "Title is required")
    private String title;
    ...
}

// Response DTOs use @Data + @AllArgsConstructor, built manually in the service
@Data
@AllArgsConstructor
public class QuestionResponse { ... }
```

### Entities

Use Lombok `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` — never write constructors/getters/setters by hand. `@Enumerated(EnumType.STRING)` for all enum columns (never `ORDINAL` — it breaks if enum order changes).

### Error handling

All exceptions are caught by the single `GlobalExceptionHandler` — no try/catch blocks in controllers. Business-rule violations (not found, invalid state, unauthorized action) throw `IllegalArgumentException` with a human-readable message; the handler converts this to **400 Bad Request** with `{ "error": "<message>" }`. This is true even for "not found" cases — the API does **not** return 404 for these; that's a deliberate simplification, not an oversight.

```java
throw new IllegalArgumentException("Session not found");
```

`MethodArgumentNotValidException` (from `@Valid` failures) returns 400 with a field-name-to-message map instead of a single `error` key.

### Authorization

Two layers, used together:

1. **Role-based**, via `@PreAuthorize("hasRole('INTERVIEWER')")` on the controller method — coarse-grained, checked before the method runs.
2. **Ownership-based**, checked manually inside the service — e.g. "is this user the interviewer who created this session?" This can't be expressed as a static role, so it's a plain `if` that throws `IllegalArgumentException` if it fails.

The currently authenticated user's email is available via `Authentication.getName()` in the controller, passed down to the service, and used to look up the `User` row when needed.

### Services own business rules

Controllers are thin — they just call one service method and wrap the result in `ResponseEntity.ok(...)`. Validation beyond `@Valid` (state checks, ownership checks, uniqueness checks) lives in the service, not the controller.

### Seeding data

One-time startup data (the default question bank) uses a `CommandLineRunner` bean in `config/`, guarded by a `count() > 0` check so it only runs once.

## Frontend (React)

### Structure

```
frontend/src/
├── api/          Axios instance with JWT request interceptor + 401 response interceptor
├── components/   Shared components (Navbar, RouteGuard)
├── context/       AuthContext — global auth state (user, login, register, logout)
└── pages/         One file per feature/page (Login, Register, QuestionBank, Sessions, Dashboard)
```

### Styling

No CSS modules or styled-components — a single `App.css` with shared utility-ish classes reused across pages: `.btn` / `.btn-primary` / `.btn-secondary` / `.btn-danger`, `.badge` (+ role/topic/difficulty variants), `.question-card`, `.modal-overlay` / `.modal`, `.form-group`. New pages should reuse these classes before inventing new ones.

### Auth

`AuthContext` holds the logged-in user (read from `localStorage` on load) and exposes `login`, `register`, `logout`. `RouteGuard` wraps any route that requires authentication and redirects to `/login` if there's no user. The Axios interceptor attaches the JWT to every request automatically and clears auth state + redirects on a 401 response — pages never handle the token manually.

### Page pattern

Each feature page is self-contained: it fetches its own data with `useEffect`, manages its own `loading`/`error` state, and calls `api.<method>('/endpoint', ...)` directly — no separate API service layer per feature. Multi-step flows within one page (e.g. starting a session, picking questions, the interview itself) are handled with a `step` state variable that switches between rendered views in the same component, rather than separate routes.

## Database

- `spring.jpa.hibernate.ddl-auto=update` — schema is managed by Hibernate from entities, no manual migrations.
- Credentials are read from `DB_USERNAME` / `DB_PASSWORD` environment variables, never hardcoded in `application.properties` (see [setup.md](setup.md)).

## Docs

Update the relevant `docs/api/*.md` and `docs/features/*.md` files as part of finishing a feature, not as a separate cleanup pass — they should always reflect what's actually built. [PLAN.md](../../PLAN.md) at the repo root is the single source of truth for what's done vs. pending.
