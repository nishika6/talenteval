# TalentEval — Claude Context

## What this project is

TalentEval is a full-stack mock interview and talent evaluation platform. Two roles exist: **Interviewer** and **Candidate**. The interviewer creates sessions and picks questions; the candidate completes the session independently (async); the interviewer reviews recordings and fills a scorecard; the candidate sees their scores and comments.

---

## Tech Stack

- **Backend:** Spring Boot 3.5.15, Spring Security, JWT (JJWT 0.12.6), Spring Data JPA, Hibernate, MySQL, Lombok, Bean Validation
- **Frontend:** React (Vite), Axios, React Router
- **Java:** 17 | **Build:** Maven | **Base package:** `com.talenteval.talenteval`
- **Working directory:** `d:\nishika original\talenteval\talenteval\`

---

## Rules — always follow these

1. **Explain before building.** For every new feature or significant change, explain the plan (what files change, what the data flow is) and wait for "yes" before writing any code.
2. **No hardcoded credentials.** DB credentials use `${DB_USERNAME}` and `${DB_PASSWORD}` env vars in `application.properties`. Never hardcode them. Never commit credentials to git.
3. **GlobalExceptionHandler always returns 400.** All `IllegalArgumentException` → 400 Bad Request, including "not found" cases. Never change this to 404.
4. **Never commit unless asked.** Nishika manages git herself. Do not run `git add`, `git commit`, or `git push` unless explicitly asked.
5. **Always use DTOs.** Entities are never returned from controllers. Every endpoint has a Request DTO (input) and Response DTO (output). `toResponse()` lives in the service layer.

---

## How to run

**Backend** (from `talenteval/` directory):
```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "yourpassword"
$env:MAVEN_OPTS = "-Xmx256m"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Xmx384m"
```
The `-Xmx384m` flag is required — the machine runs out of memory without it.

**Frontend** (from `talenteval/frontend/` directory):
```powershell
npm run dev
```
Vite may start on port 5173 or 5174 (if 5173 is taken). Both are allowed in `SecurityConfig` CORS.

**Common errors:**
- "Port 8080 already in use" → `taskkill /F /IM java.exe`
- DB connection fails → env vars not set in current PowerShell session, set them again
- `npm error Missing script: dev` → wrong directory, must be inside `talenteval/frontend/`

---

## Branch state

| Branch | Contains |
|---|---|
| `main` | Feature 1 (auth) + docs folder (README, docs/, conventions.md, PLAN.md, CLAUDE.md) |
| `feature/dashboard` | Superseded — Features 1–5 + dark mode. Left as-is, not used going forward. |
| `feature/email-notifications` | Stale/superseded — branched off `main` early, missing everything from `feature/dashboard` onward. Do not merge, it would delete newer work. |
| `feature/scheduler` | **Current tip of development.** All 8 features + dark mode + scheduler + email notifications + upcoming-sessions dashboard. |

All active development is on `feature/scheduler`. `CLAUDE.md` and other docs live only on `main` (feature branches don't carry them) — when merging a feature branch into `main`, expect doc-only merge conflicts on files like `PLAN.md`; they are normal and resolvable by keeping `main`'s side and re-adding the feature-branch content.

---

## Features built

### 1. User Authentication ✅
- Register/login with JWT (stateless, 24h expiry)
- BCrypt password hashing
- Roles: `INTERVIEWER`, `CANDIDATE`
- JWT stored in `localStorage`, attached via Axios request interceptor
- Auto-logout on 401 via Axios response interceptor
- Route guard (`RouteGuard.jsx`) redirects unauthenticated users

### 2. Question Bank ✅
- CRUD: interviewers can add/edit/delete; candidates read-only
- Fields: title, role (HR/UX/PM/FINANCE/ENGINEERING), topic, difficulty (EASY/MEDIUM/HARD)
- Filter by role and/or topic (Spring Data method-name queries, no SQL written)
- 25 starter questions seeded automatically on first startup (`QuestionSeeder.java`)

### 3. Mock Interview Sessions ✅ (async flow)
- Interviewer creates session + picks questions → candidate gets email notification
- Candidate records verbal answers independently, then completes session → interviewer gets email
- Interviewer reviews recordings and fills scorecard afterward
- `SessionQuestion` is a separate entity (not `@ManyToMany`) to store `questionOrder`
- `addQuestions()` clears and re-inserts all questions each time (no diffing)

### 4. Scorecard ✅
- 4 criteria: Communication, Structure, Content, Confidence (each 1–5)
- Optional written comments field
- One scorecard per session enforced at DB level (`unique=true` on `session_id`)
- Interviewer only can submit; both interviewer and candidate can view

### 5. Progress Dashboard ✅
- No new DB table — pure aggregation over existing `Scorecard` data
- Candidate view: summary cards (total sessions + 4 criterion averages), trend bar chart, session history with scores and interviewer comments
- Interviewer view: candidate picker dropdown, then same view
- `round1()` = `Math.round(v * 10.0) / 10.0` for 1-decimal averages

### 6. Show Comments to Candidate ✅
- Interviewer's written scorecard comments now appear in the candidate's Dashboard under each session in the Session History
- `SessionProgressResponse` DTO has `comments` field; `ProgressService` maps `sc.getComments()`
- Comments are conditionally rendered in `Dashboard.jsx` only when non-null

### 7. Scheduler ✅
- `scheduledAt` (nullable `LocalDateTime`) added to `InterviewSession` entity
- Interviewer picks date & time via a `datetime-local` input when creating a session
- Session list cards and session view show scheduled time if set, creation time (`date`) otherwise

### 8. Email Notifications ✅
- `spring-boot-starter-mail` + `JavaMailSender`, SMTP config entirely via env vars (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`)
- `@EnableAsync` on `TalentevalApplication` — `EmailService` methods are `@Async`, emails send in the background and never block the API response
- `EmailService.notifyCandidate()` fires on session creation (includes scheduled time if set); `EmailService.notifyInterviewer()` fires when the candidate completes their session
- This is what made the session-completion flow async on both sides: `PUT /sessions/{id}/complete` has no `@PreAuthorize` — `SessionService.completeSession()` now checks the caller's role internally and allows either the interviewer or the candidate to complete a session, branching only the ownership check and the "who gets emailed" logic
- Candidate sees a "Complete Session" button on the last question of their session view; completing returns them to the session list, while an interviewer completing goes straight to the scorecard form

### 9. Upcoming Sessions on Dashboard ✅
- No backend changes — pure frontend aggregation over the existing `GET /sessions` response
- Candidate view: "Upcoming Sessions" section — sessions that are scheduled and not yet completed, with interviewer name, scheduled date/time, and a "Go to Session" button
- Interviewer view: separate "Upcoming Sessions" and "Recently Completed" (last 5) sections, each with candidate name, date/time, status badge, and a "View Session" button — kept as two distinct sections rather than one merged list so a "COMPLETED" badge never shows up under an "Upcoming" heading
- Buttons navigate to `/sessions` passing the target session id via React Router `state` (`{ openSessionId }`); `Sessions.jsx` reads `location.state.openSessionId` on mount and auto-opens that session

---

## Features in progress

### Voice Recording (async model) — TODO
- Candidate records verbal answers using browser `MediaRecorder` API during their session
- Start/Stop Recording button per question on candidate's session page
- Audio blobs uploaded to backend on session completion (multipart)
- Stored in local server storage; new `SessionRecording` entity tracks (sessionId, questionId, filePath)
- Interviewer sees audio playback per question in session review page before filling scorecard

---

## Key architecture decisions

### Two-layer authorization
Most write operations on sessions/scorecards have two checks:
1. `@PreAuthorize("hasRole('INTERVIEWER')")` on the controller — role check from JWT, cheap
2. Manual ownership check in the service layer — "is this caller the interviewer of *this* session?" — requires a DB query

Exception: `PUT /sessions/{id}/complete` has no `@PreAuthorize` since either role can complete a session. `SessionService.completeSession()` does the role check itself (interviewer must own the session vs. candidate must be the session's candidate) and only emails the interviewer when a candidate is the one completing it.

### Security config
- `/api/auth/**` → public (no JWT required)
- All other routes → require valid JWT
- CORS allows: `http://localhost:5173` and `http://localhost:5174`
- Sessions are stateless (`SessionCreationPolicy.STATELESS`) — no server-side session storage
- `@EnableMethodSecurity` enables `@PreAuthorize` on controllers

### JWT filter (`JwtAuthenticationFilter`)
Extends `OncePerRequestFilter`. On every request: reads `Authorization: Bearer <token>` header → validates signature + expiry → extracts email → loads `UserDetails` from DB → sets `SecurityContextHolder`. If no valid token, passes through without setting auth (protected routes then reject with 401).

### CSS variables (dark mode)
All colors in `App.css` use `var(--...)` variables defined in `index.css :root`. No hardcoded hex values in stylesheets. `color-scheme: dark` on `:root`. Button primary text is `#0f1115` (dark) because `--primary` is light indigo `#818cf8`.

### SessionQuestion entity
Separate `@Entity` (not `@ManyToMany`) so `questionOrder` can be stored as a column. Parent uses `@OrderBy("questionOrder ASC")` so questions always come back sorted from the DB query.

### ProgressService
No new table. Fetches all `Scorecard` rows for a candidate ordered by session date, computes per-criterion averages with `round1()`, returns `ProgressResponse` with a list of `SessionProgressResponse` (one per scored session, includes comments).

### EmailService
`@Service` wrapping `JavaMailSender`, both public methods marked `@Async` so callers (`SessionService`) never wait on SMTP. Uses `SimpleMailMessage` (plain text, no templates). Failures are not currently caught/retried — an SMTP error surfaces as an async exception logged by Spring, it does not fail the originating request since it's already returned.

### Dashboard "Upcoming Sessions"
`Dashboard.jsx` fetches `GET /sessions` (same endpoint `Sessions.jsx` uses) and derives `upcoming` / `recentlyCompleted` client-side with `.filter()`/`.sort()` — no new DTO or endpoint. Kept as a derived-on-render computation rather than `useMemo` since the session list is small and this isn't a perf-sensitive path.

---

## DB schema (auto-managed by Hibernate `ddl-auto=update`)

| Table | Key columns |
|---|---|
| `users` | id, name, email (unique), password (BCrypt), role |
| `questions` | id, title, role, topic, difficulty |
| `sessions` | id, interviewer_id, candidate_id, date, scheduled_at, status |
| `session_questions` | id, session_id, question_id, question_order |
| `scorecards` | id, session_id (unique), candidate_id, communication, structure, content, confidence, comments |

---

## application.properties settings

```
spring.datasource.url=jdbc:mysql://localhost:3306/talenteval
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
jwt.secret=TalentEvalSuperSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm
jwt.expiration=86400000

# Mail
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Local secrets (DB + mail credentials) are kept in a git-ignored `application-local.properties`, run with `-Dspring-boot.run.profiles=local` or equivalent, rather than typed into every PowerShell session by hand.
