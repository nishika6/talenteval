# TalentEval — Project Plan

## Current Status
- Feature: 8 features complete
- Last completed: Feature 8 — Upcoming Sessions on Dashboard

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

### 2. Question Bank (built on `feature/question-bank`, tested end-to-end — not yet merged to main)
- [x] Question entity (title, role, topic, difficulty)
- [x] QuestionRepository
- [x] QuestionService + QuestionController (CRUD for interviewers, read-only for candidates)
- [x] Database seeder — pre-seed default questions across all roles
- [x] React question bank page for interviewers (add / edit / delete)
- [x] React question browse page for candidates (filter by role + topic)

### 3. Mock Interview Session (built on `feature/mock-interview`, tested end-to-end — not yet merged to main)
- [x] Session entity (interviewer, candidate, date, questions used, status)
- [x] SessionRepository + SessionService + SessionController
- [x] API: start session, add questions, mark complete
- [x] React interviewer flow: select candidate -> pick questions -> guided question-by-question view
- [x] React candidate view: see active session questions

### 4. Scorecard (built on `feature/scorecard`, tested end-to-end — not yet merged to main)
- [x] Scorecard entity (session, ratings for Communication / Structure / Content / Confidence, comments)
- [x] ScorecardRepository + ScorecardService + ScorecardController
- [x] API: submit scorecard, fetch scorecard by session
- [x] React scorecard form for interviewers (4 criteria + comments)
- [x] React scorecard view for candidates (read-only)

### 5. Progress Dashboard (built on `feature/dashboard`, tested end-to-end — not yet merged to main)
- [x] Progress API (all sessions + scores per candidate)
- [x] React candidate dashboard: past sessions, scores per criteria, improvement over time
- [x] React interviewer view: candidate history across sessions

### 6. Scheduler
- [x] Added `scheduledAt` field (nullable LocalDateTime) to InterviewSession entity
- [x] SessionRequest accepts optional `scheduledAt` from frontend
- [x] SessionResponse includes `scheduledAt` in all session responses
- [x] Interviewer picks date & time when selecting a candidate (datetime-local input)
- [x] Session list cards show scheduled time if set, creation time otherwise
- [x] Session view shows scheduled time in session-info row for both roles

### 7. Email Notifications
- [x] Added spring-boot-starter-mail dependency
- [x] SMTP configured via env vars: MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD
- [x] @EnableAsync on main app class — emails send in background, don't block API
- [x] EmailService: notifyCandidate() — fires on session creation, includes scheduled time if set
- [x] EmailService: notifyInterviewer() — fires when candidate completes their session
- [x] Async session flow: candidates can now call PUT /sessions/{id}/complete
- [x] Candidate "Complete Session" button shown on last question of their session view
- [x] After candidate completes: returns to session list; after interviewer completes: scorecard form

### 8. Upcoming Sessions on Dashboard
- [x] Dashboard fetches all sessions via existing GET /sessions endpoint (no backend changes needed)
- [x] Candidate view: "Upcoming Sessions" section shows sessions that are scheduled and not yet completed, with interviewer name, scheduled date/time, and a "Go to Session" button
- [x] Interviewer view: same section shows upcoming sessions plus the 5 most recently completed, with candidate name, scheduled date/time, status badge, and a "View Session" button
- [x] Buttons navigate to /sessions passing the target session id via route state; Sessions.jsx auto-opens that session on mount

## Docs
- [x] docs folder created with all documents
- [x] Root README.md (entry point for new developers)
- [x] docs/conventions.md (coding conventions used throughout the codebase)
- [x] setup.md updated to reflect DB_USERNAME/DB_PASSWORD env vars (was hardcoded password)
- [x] Fixed incorrect 404 status codes in question-api.md, scorecard-api.md, progress-api.md (actual behavior is 400 — GlobalExceptionHandler always returns 400 for IllegalArgumentException)
- [x] API + feature docs kept in sync with actual implementation for all 5 features

## Docs
- [x] docs folder created with all documents

## Decisions & Notes
- Project generated via start.spring.io with: Spring Web, Spring Security, Spring Data JPA, MySQL Driver, Lombok, Validation
- Java 17, Spring Boot 3.5.15, Maven
- Base package: com.talenteval.talenteval (as generated by start.spring.io)
- After completing backend for each feature, update the corresponding 
  API doc in docs/api/ with the actual endpoints built
