# TalentEval — Claude Context

## What this project is

TalentEval is a full-stack mock interview and talent evaluation platform. Two roles exist: **Interviewer** and **Candidate**. The interviewer creates sessions and picks questions; the candidate completes the session independently (async); the interviewer reviews recordings and fills a scorecard; the candidate sees their scores and comments.

---

## Tech Stack

- **Backend:** Spring Boot 3.5.15, Spring Security, JWT (JJWT 0.12.6), Spring Data JPA, Hibernate, MySQL, Lombok, Bean Validation
- **Frontend:** React (Vite), Axios, React Router
- **Java:** 17 | **Build:** Maven | **Base package:** `com.talenteval.talenteval`
- **Working directory:** `c:\Users\amana\Downloads\talenteval\talenteval\`

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
| `main` | Feature 1 (auth) + docs folder (README, docs/, conventions.md, PLAN.md) |
| `feature/dashboard` | All 5 features + dark mode (dark mode not yet committed as of last session) |

All active development is on `feature/dashboard`. When merging into `main`, expect doc merge conflicts — they are normal and resolvable.

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

---

## Features in progress

### Voice Recording (async model) — TODO
- Candidate records verbal answers using browser `MediaRecorder` API during their session
- Start/Stop Recording button per question on candidate's session page
- Audio blobs uploaded to backend on session completion (multipart)
- Stored in local server storage; new `SessionRecording` entity tracks (sessionId, questionId, filePath)
- Interviewer sees audio playback per question in session review page before filling scorecard

### Email Notifications — TODO
- `spring-boot-starter-mail` + `JavaMailSender`
- SMTP config via env vars (never hardcoded)
- Email to candidate when session is assigned
- Email to interviewer when candidate completes their session

---

## Key architecture decisions

### Two-layer authorization
Every write operation on sessions/scorecards has two checks:
1. `@PreAuthorize("hasRole('INTERVIEWER')")` on the controller — role check from JWT, cheap
2. Manual ownership check in the service layer — "is this caller the interviewer of *this* session?" — requires a DB query

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

---

## DB schema (auto-managed by Hibernate `ddl-auto=update`)

| Table | Key columns |
|---|---|
| `users` | id, name, email (unique), password (BCrypt), role |
| `questions` | id, title, role, topic, difficulty |
| `sessions` | id, interviewer_id, candidate_id, date, status |
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
```
