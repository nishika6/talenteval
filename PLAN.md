# TalentEval — Project Plan

## Current Status
- Feature: 10 features complete + 3 fixes
- Last completed: Forgot Password
- `main` is fully up to date — all features and fixes below are merged into `main`

## Features

### 1. User Authentication
- [x] Spring Boot project init (done via start.spring.io)
- [x] Configure application.properties (MySQL + JWT settings)
- [x] User entity with CANDIDATE / INTERVIEWER roles
- [x] JWT utility (generate + validate tokens)
- [x] JWT filter (runs on every request)
- [x] Spring Security config (public vs protected routes, CORS)
- [x] UserDetailsServiceImpl
- [x] Register + Login endpoints with DTOs
- [x] GlobalExceptionHandler
- [x] React login + register pages (role selection on register)
- [x] JWT stored in localStorage, Axios interceptor added
- [x] Route guard (redirect if not logged in)

### 2. Question Bank
- [x] Question entity (title, role, topic, difficulty)
- [x] QuestionRepository
- [x] QuestionService + QuestionController (CRUD for interviewers, read-only for candidates)
- [x] Database seeder — pre-seed default questions across all roles
- [x] React question bank page for interviewers (add / edit / delete)
- [x] React question browse page for candidates (filter by role + topic)

### 3. Mock Interview Session
- [x] Session entity (interviewer, candidate, date, questions used, status)
- [x] SessionRepository + SessionService + SessionController
- [x] API: start session, add questions, mark complete
- [x] React interviewer flow: select candidate -> pick questions -> guided question-by-question view
- [x] React candidate view: see active session questions

### 4. Scorecard
- [x] Scorecard entity (session, ratings for Communication / Structure / Content / Confidence, comments)
- [x] ScorecardRepository + ScorecardService + ScorecardController
- [x] API: submit scorecard, fetch scorecard by session
- [x] React scorecard form for interviewers (4 criteria + comments)
- [x] React scorecard view for candidates (read-only)

### 5. Progress Dashboard
- [x] Progress API (all sessions + scores per candidate)
- [x] React candidate dashboard: past sessions, scores per criteria, improvement over time
- [x] React interviewer view: candidate history across sessions

### 6. Show Comments to Candidate
- [x] `SessionProgressResponse` DTO has a `comments` field
- [x] `ProgressService` maps `sc.getComments()` onto each session
- [x] Comments rendered in candidate Dashboard's Session History, only when non-null

### 7. Scheduler
- [x] Added `scheduledAt` field (nullable LocalDateTime) to InterviewSession entity
- [x] SessionRequest accepts optional `scheduledAt` from frontend
- [x] SessionResponse includes `scheduledAt` in all session responses
- [x] Interviewer picks date & time when selecting a candidate (datetime-local input)
- [x] Session list cards show scheduled time if set, creation time otherwise
- [x] Session view shows scheduled time in session-info row for both roles

### 8. Email Notifications
- [x] Added spring-boot-starter-mail dependency
- [x] SMTP configured via env vars: MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD
- [x] @EnableAsync on main app class — emails send in background, don't block API
- [x] EmailService: notifyCandidate() — fires on session creation, includes scheduled time if set
- [x] EmailService: notifyInterviewer() — fires when candidate completes their session
- [x] Async session flow: candidates can now call PUT /sessions/{id}/complete
- [x] Candidate "Complete Session" button shown on last question of their session view
- [x] After candidate completes: returns to session list; after interviewer completes: scorecard form

### 9. Upcoming Sessions on Dashboard
- [x] Dashboard fetches all sessions via existing GET /sessions endpoint (no backend changes needed)
- [x] Candidate view: "Upcoming Sessions" section shows sessions that are scheduled and not yet completed, with interviewer name, scheduled date/time, and a "Go to Session" button
- [x] Interviewer view: same section shows upcoming sessions plus the 5 most recently completed, with candidate name, scheduled date/time, status badge, and a "View Session" button
- [x] Buttons navigate to /sessions passing the target session id via route state; Sessions.jsx auto-opens that session on mount

### 10. Voice Recording
- [x] `SessionRecording` entity (session, question, filePath, uploadedAt) — one row per session+question, unique constraint on the pair
- [x] `RecordingStorageService` interface (`store()`/`load()`) abstracts the storage backend
- [x] Candidate records verbal answers via browser `MediaRecorder` API (`audio/webm`), one recording per question
- [x] Recording is **required** before a candidate can complete their session (`SessionService.completeSession()` checks `RecordingService.isFullyRecorded()`)
- [x] Interviewer sees `<audio controls>` playback per question on the session review page, before filling the scorecard
- [x] Started with local filesystem storage (`LocalRecordingStorageService`), confirmed working end-to-end
- [x] Migrated to **Cloudinary** cloud storage (`CloudinaryRecordingStorageService`) after a senior flagged that local files risk data loss and don't scale for concurrent users; local implementation removed once Cloudinary was confirmed working
- [x] Frontend never talks to Cloudinary directly — `GET /sessions/{id}/recordings/{questionId}/audio` is an authenticated proxy endpoint that fetches from Cloudinary server-side and streams bytes back

## Fixes

Flagged by a senior code review; fixed in this order.

### Fix 1: Website Link in Emails
- [x] Added `app.frontend-url` config (`FRONTEND_URL` env var, defaults to `http://localhost:5173`)
- [x] `EmailService.notifyCandidate()` and `notifyInterviewer()` both now include a link to `/login`

### Fix 2: Email Alias Bypass
- [x] `AuthService.normalizeEmail()` lowercases the address and strips everything from `+` onward in the local part
- [x] Applied before `existsByEmail` in `register()` and before `authenticate()`/`findByEmail` in `login()`, so `nishika+abc@gmail.com` can no longer be used to create a second account for `nishika@gmail.com`

### Fix 3: Forgot Password
- [x] `PasswordResetToken` entity (token, user, expiryDate, used) + `PasswordResetTokenRepository`
- [x] `POST /api/auth/forgot-password` — generates a single-use token with a 30-minute expiry, emails a reset link; always returns the same generic response so the API never reveals whether an email is registered
- [x] `POST /api/auth/reset-password` — validates the token (not expired/used), updates the password, marks the token used
- [x] New frontend pages `ForgotPassword.jsx` and `ResetPassword.jsx`, routes added to `App.jsx`, "Forgot Password?" link added to `Login.jsx`

## Docs
- [x] docs folder created with all documents

## Decisions & Notes
- Project generated via start.spring.io with: Spring Web, Spring Security, Spring Data JPA, MySQL Driver, Lombok, Validation
- Java 17, Spring Boot 3.5.15, Maven
- Base package: com.talenteval.talenteval (as generated by start.spring.io)
- After completing backend for each feature, update the corresponding
  API doc in docs/api/ with the actual endpoints built
