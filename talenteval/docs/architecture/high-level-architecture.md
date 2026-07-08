# High-Level Architecture — TalentEval

## System Overview

TalentEval is a three-tier application with a React single-page application (SPA) frontend, a Spring Boot REST API backend, and a MySQL relational database.

```
┌─────────────────────────────────┐
│     React SPA (Vite)            │
│     http://localhost:5173       │
│                                  │
│  Pages: Login, Register,        │
│  Forgot/Reset Password,         │
│  Question Bank, Sessions        │
│  (incl. scorecard), Dashboard   │
│                                  │
│  Axios HTTP Client               │
│  JWT stored in localStorage      │
└────────────┬─────────────────────┘
             │  HTTP (JSON + multipart for recordings)
             │  Authorization: Bearer <JWT>
             ▼
┌───────────────────────────────────┐
│   Spring Boot REST API            │
│   http://localhost:8080           │
│                                   │
│  ┌───────────────────────────┐   │
│  │   Security Layer          │   │
│  │  ┌─────────────────────┐  │   │
│  │  │ CORS Filter          │  │   │
│  │  │ JWT Auth Filter      │  │   │
│  │  │ SecurityConfig       │  │   │
│  │  └─────────────────────┘  │   │
│  └───────────┬───────────────┘   │
│              ▼                   │
│  ┌───────────────────────────┐   │
│  │   Controller Layer        │   │
│  │  AuthController           │   │
│  │  QuestionController       │   │
│  │  SessionController        │   │
│  │  RecordingController      │   │
│  │  ScorecardController      │   │
│  │  ProgressController       │   │
│  └───────────┬───────────────┘   │
│              ▼                   │
│  ┌───────────────────────────┐   │
│  │   Service Layer           │   │
│  │  Business logic,          │   │
│  │  validation, mapping      │   │
│  │  EmailService (@Async)    │   │
│  │  RecordingStorageService  │───┼──▶ Cloudinary (audio storage)
│  └───────────┬───────────────┘   │
│              ▼                   │
│  ┌───────────────────────────┐   │
│  │   Repository Layer        │   │
│  │  Spring Data JPA          │   │
│  └───────────┬───────────────┘   │
└──────────────┼───────────────────┘
               │  JDBC                  ─────▶ SMTP (session/reset emails)
               ▼
┌───────────────────────────────────┐
│       MySQL 8 Database            │
│       localhost:3306              │
│       Schema: talenteval          │
│                                   │
│  Tables: users, questions,        │
│  sessions, session_questions,     │
│  scorecards, session_recordings,  │
│  password_reset_tokens            │
└───────────────────────────────────┘
```

## Request Flow

A typical authenticated request flows through the system like this:

1. **React** sends an HTTP request with `Authorization: Bearer <token>` header via Axios.
2. **CORS Filter** checks that the request origin (`localhost:5173`) is allowed.
3. **JwtAuthenticationFilter** extracts the token from the header, validates it, loads the user from the database, and sets the authentication in Spring Security's context.
4. **SecurityConfig** checks if the request is allowed — public routes (`/api/auth/**`) pass through without a token; all others require authentication. Role-based access is enforced with `@PreAuthorize` on individual controller methods.
5. **Controller** receives the request, validates the DTO using Jakarta Validation annotations, and delegates to the service layer.
6. **Service** applies business logic (e.g., "only the interviewer who created a session can submit a scorecard"), interacts with repositories, and returns a response DTO.
7. **Repository** executes the JPA query against MySQL via Hibernate.
8. The response flows back as JSON to the React frontend.

If any step fails, **GlobalExceptionHandler** catches the exception and returns a structured JSON error response with an appropriate HTTP status code.

## Security Layer

### JWT Authentication

- On login or registration, the backend generates a JWT containing the user's email and role, signed with an HMAC-SHA256 secret key.
- The token is valid for 24 hours.
- The frontend stores the token in `localStorage` and attaches it to every request via an Axios interceptor.
- The `JwtAuthenticationFilter` (a `OncePerRequestFilter`) runs before every request, validates the token, and sets up the Spring Security context.

### Authorization

- **Public routes:** everything under `/api/auth/**` — `register`, `login`, `forgot-password`, `reset-password`.
- **Authenticated routes:** All other `/api/**` endpoints require a valid JWT.
- **Role-based access:** Individual endpoints use `@PreAuthorize("hasRole('INTERVIEWER')")` or `@PreAuthorize("hasRole('CANDIDATE')")` to restrict access by role. `PUT /api/sessions/{id}/complete` is the one exception — it has no `@PreAuthorize` since either role can complete a session; `SessionService` checks the caller's role and relationship to the session itself.

### CORS

- Configured to allow requests from `http://localhost:5173` and `http://localhost:5174` (Vite falls back to 5174 if 5173 is taken).
- Allows GET, POST, PUT, DELETE, and OPTIONS methods.
- Credentials (cookies, authorization headers) are permitted.

### Async email

`EmailService`'s methods are `@Async` (enabled via `@EnableAsync` on the main application class), so sending a session-assignment, completion-notification, or password-reset email never blocks the API response. If SMTP fails, the exception is only logged — the original request has already returned successfully by then.

## Package Structure

```
com.talenteval.talenteval
├── controller/       # REST controllers (one per feature)
├── dto/              # Request and response DTOs
├── entity/           # JPA entities mapped to database tables
├── exception/        # GlobalExceptionHandler
├── repository/       # Spring Data JPA repository interfaces
├── security/         # JWT utility, filter, UserDetailsService, SecurityConfig
└── service/          # Business logic services
```

## Frontend Structure

```
frontend/src/
├── api/              # Axios instance with JWT interceptor
├── components/       # Shared components (RouteGuard, etc.)
├── context/          # React context providers (AuthContext)
├── pages/            # Page components (Login, Register, Dashboard, etc.)
├── App.jsx           # Route definitions
├── App.css           # Component styles
├── index.css         # Global styles and CSS variables
└── main.jsx          # Entry point with BrowserRouter
```
