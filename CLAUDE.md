# TalentEval — Claude Context

## What this project is

TalentEval is a full-stack mock interview and talent evaluation platform. Two roles exist: **Interviewer** and **Candidate**. The interviewer creates sessions and picks questions; the candidate completes the session independently (async); the interviewer reviews recordings and fills a scorecard; the candidate sees their scores and comments.

---

## Tech Stack

- **Backend:** Spring Boot 3.5.15, Spring Security, JWT (JJWT 0.12.6), Spring Data JPA, Hibernate, MySQL, Lombok, Bean Validation, Spring Mail, Cloudinary SDK
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
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Xmx384m" "-Dspring-boot.run.profiles=local"
```
Credentials (DB, mail, Cloudinary) come from `application-local.properties` (git-ignored) via the `local` profile — not from PowerShell env vars set by hand. The `-Xmx384m` flag caps JVM heap to keep memory usage low; if memory ever runs out on startup, this is the flag to add or increase.

**Frontend** (from `talenteval/frontend/` directory):
```powershell
npm run dev
```
Vite may start on port 5173 or 5174 (if 5173 is taken). Both are allowed in `SecurityConfig` CORS.

**Common errors:**
- "Port 8080 already in use" → `taskkill /F /IM java.exe`
- DB connection fails → check `application-local.properties` exists and has correct values, and that `-Dspring-boot.run.profiles=local` was passed
- `npm error Missing script: dev` → wrong directory, must be inside `talenteval/frontend/`

---

## Branch state

`main` is the source of truth and is fully up to date. Features 1–10 were merged into `main` via GitHub PRs (#1–#7), the 3 post-review fixes were committed directly onto `main` afterward, and Feature 11 (Time Limit for Questions) was committed directly onto `main` as well. The feature branches used to build 1–10 have since been deleted (merged and no longer needed) — `main` is the only branch that matters going forward.

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

### 10. Voice Recording ✅
- Candidate records verbal answers per question via browser `MediaRecorder` API (`audio/webm`) during their session; recording is **required** before completing — `SessionService.completeSession()` calls `RecordingService.isFullyRecorded()` and blocks candidates who haven't recorded every question
- `SessionRecording` entity tracks (session, question, filePath, uploadedAt) — `filePath` holds the Cloudinary `secure_url`, not a local disk path
- `RecordingStorageService` interface (`store()`/`load()`) abstracts the storage backend — implemented by `CloudinaryRecordingStorageService`. Originally built against local disk (`LocalRecordingStorageService`), confirmed working, then migrated to Cloudinary after a senior flagged that local files risk data loss and don't scale for concurrent users; the local implementation was removed once Cloudinary was confirmed working
- Audio uploaded to Cloudinary with `resource_type: "video"` (Cloudinary has no dedicated audio type)
- Frontend never talks to Cloudinary directly — `GET /sessions/{id}/recordings/{questionId}/audio` is an authenticated proxy endpoint that fetches bytes from Cloudinary server-side and streams them back, preserving the existing "only session participants can access" authorization model
- Interviewer sees `<audio controls>` playback per question on the session review page before filling the scorecard
- **Superseded by Feature 12** — audio-only recording was upgraded to video; see below.

### 11. Time Limit for Questions ✅
- `Question.timeLimit` (int, seconds), defaults to `120` via `@Builder.Default` + a DB column default, so `ddl-auto=update` could add the column without breaking the 25 already-seeded questions
- Interviewers set it on the Question Bank add/edit form (10–1800s); `QuestionRequest`/`QuestionResponse` and `SessionQuestionResponse` all carry it through
- Candidate's session page counts down from the question's `timeLimit` once recording starts, turns red in the last 30 seconds, and at 0 auto-stops the recording (same path as a manual stop) and advances to the next question — except on the last question, where it just stops/uploads and leaves the candidate there
- The auto-stop/advance logic lives in a `useEffect` watching `timeLeft`, not inside the `setTimeLeft` updater — `main.jsx` wraps the app in `<StrictMode>`, which can invoke updater functions more than once, risking a double-upload or skipping two questions instead of one
- Considered adding a per-session override on top of the question's default (editable when picking questions for a session) but declined it — the Question Bank's existing "set once, reuse everywhere" model already covered what was actually requested, and an override would've added a nullable `SessionQuestion.timeLimitOverride` column and a more complex picker UI for a scenario nobody asked for

### 12. Video Recording (upgrade from Voice Recording) ✅
- Candidate's `getUserMedia({ video: true, audio: true })` replaces the old audio-only capture; `MediaRecorder` and the uploaded blob use `video/webm`
- Live, muted self-preview `<video>` shown only while actively recording — wired via a `useEffect` watching `isRecording`, not set inline inside `startRecording()`, since the preview element doesn't exist in the DOM yet at that point on the very first recording (it's only mounted once `isRecording` becomes true)
- Interviewer's playback swapped from `<audio controls>` to `<video controls>`
- `RecordingController`/`RecordingService.getAudio()` renamed to `getVideo()`, endpoint `/recordings/{questionId}/audio` renamed to `/video`, response `Content-Type` changed from `audio/webm` to `video/webm`
- `spring.servlet.multipart.max-file-size`/`max-request-size` raised from 25MB to 100MB, since video is significantly larger than audio-only at the same duration
- Two things this upgrade did **not** require, worth knowing if asked: no Cloudinary config change (uploads already used `resource_type: "video"`, since Cloudinary has no dedicated audio type) and no DB migration (`SessionRecording.filePath` was always a generic URL column, agnostic to content type)
- `docs/features/voice-recording.md` renamed to `video-recording.md`, all cross-references updated

---

## Fixes from senior review

1. **Website link in emails** — `app.frontend-url` config (`${FRONTEND_URL:http://localhost:5173}`) injected into `EmailService`; both `notifyCandidate()` and `notifyInterviewer()` now append a link to `/login`.
2. **Email alias bypass** — Gmail-style `+alias` addresses (e.g. `nishika+abc@gmail.com`) could register as a separate account from `nishika@gmail.com` despite delivering to the same inbox. `AuthService.normalizeEmail()` lowercases the address and strips everything from `+` onward in the local part; applied before every `existsByEmail`/`findByEmail` lookup in `register()` and `login()`. Scoped to `+`-stripping only (not Gmail dot-insensitivity, which would risk false-colliding legitimately distinct addresses on other providers). Existing accounts created before this fix are not migrated/merged.
3. **Forgot Password** — `PasswordResetToken` entity (token, user, expiryDate, used) + `PasswordResetTokenRepository`. `POST /api/auth/forgot-password` issues a 30-minute single-use token and emails a reset link, but always returns the same generic response regardless of whether the email exists (prevents email enumeration). `POST /api/auth/reset-password` validates the token isn't expired/used, updates the password, and marks it used. Both endpoints fall under the existing `/api/auth/**` permitAll rule — no `SecurityConfig` changes needed. New frontend pages `ForgotPassword.jsx`/`ResetPassword.jsx`, routes added to `App.jsx`, link added to `Login.jsx`. No cleanup job for expired/used tokens — they just accumulate in the table; acceptable for now.

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

### RecordingStorageService (Cloudinary)
`store()`/`load()` interface designed specifically so swapping storage backends is a single-class change — no entity/controller/frontend changes needed when migrating from local disk to Cloudinary. `CloudinaryRecordingStorageService` uploads with `resource_type: "video"` and `load()` re-fetches bytes from the Cloudinary `secure_url` via `java.net.http.HttpClient` for the authenticated proxy endpoint.

### Email normalization
`AuthService.normalizeEmail()` lowercases and strips `+alias` suffixes before every register/login lookup. Deliberately does not strip dots — that's a Gmail-only quirk, and applying it to all providers would risk false-colliding legitimately distinct addresses elsewhere.

---

## DB schema (auto-managed by Hibernate `ddl-auto=update`)

| Table | Key columns |
|---|---|
| `users` | id, name, email (unique), password (BCrypt), role |
| `questions` | id, title, role, topic, difficulty, time_limit (default 120) |
| `sessions` | id, interviewer_id, candidate_id, date, scheduled_at, status |
| `session_questions` | id, session_id, question_id, question_order |
| `scorecards` | id, session_id (unique), candidate_id, communication, structure, content, confidence, comments |
| `session_recordings` | id, session_id + question_id (unique together), file_path (Cloudinary URL), uploaded_at |
| `password_reset_tokens` | id, token (unique), user_id, expiry_date, used |

---

## application.properties settings

```
spring.datasource.url=jdbc:mysql://localhost:3306/talenteval?createDatabaseIfNotExist=true
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

# Voice recordings
spring.servlet.multipart.max-file-size=25MB
spring.servlet.multipart.max-request-size=25MB

# Cloudinary
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}

# Frontend
app.frontend-url=${FRONTEND_URL:http://localhost:5173}
```

Local secrets (DB + mail + Cloudinary credentials) are kept in a git-ignored `application-local.properties`, run with `-Dspring-boot.run.profiles=local` or equivalent, rather than typed into every PowerShell session by hand.

**Known gap:** `spring.datasource.url` and `jwt.secret` are still hardcoded above rather than env vars (unlike `DB_USERNAME`/`DB_PASSWORD`/mail/Cloudinary). This was flagged and a fix was designed (`${DB_URL:...}` and `${JWT_SECRET}`), but deliberately deferred — rotating `jwt.secret` would invalidate every currently-issued JWT and log all users out, so it's being done as its own separate step, not bundled into another change.
