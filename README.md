# TalentEval

A role-agnostic mock interview and talent evaluation platform. An interviewer creates a session for a candidate and picks questions; the candidate records a spoken answer for each question independently, on their own time; the interviewer reviews the recordings and fills a structured scorecard; the candidate tracks their scores and feedback over time. TalentEval is interviewer-led but asynchronous — the two participants don't need to be online at the same time.

## Roles

- **INTERVIEWER** — starts sessions (optionally scheduled), picks questions, reviews recordings, fills scorecards, views all candidate progress
- **CANDIDATE** — records answers for their session, completes it once every question is recorded, views their own past sessions, scores, and progress

## Features

1. **User Authentication** — email/password login (with alias-proof email normalization), JWT sessions, BCrypt hashing, role selected at registration, self-service password reset via email
2. **Question Bank** — questions tagged by role (HR/UX/PM/Finance/Engineering), topic, and difficulty; interviewers manage them, candidates browse read-only
3. **Mock Interview Session** — interviewer selects a candidate and picks questions; candidate works through them independently and completes the session once every question is recorded
4. **Scorecard** — interviewer rates the candidate 1-5 on Communication, Structure, Content, and Confidence, with optional comments visible to the candidate
5. **Progress Dashboard** — candidates (and interviewers, for any candidate) see score trends across sessions over time, plus upcoming/scheduled sessions
6. **Scheduler** — interviewer can set a date/time for a session at creation
7. **Email Notifications** — candidate is emailed when assigned a session, interviewer is emailed when the candidate completes it
8. **Voice Recording** — candidate records a spoken answer per question via the browser microphone; stored in Cloudinary, played back by the interviewer through an authenticated proxy endpoint

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.5.15, Spring Security, JWT (JJWT), MySQL 8, Spring Data JPA / Hibernate, Spring Mail, Cloudinary
- **Frontend:** React (Vite), Axios, React Router

## Project Structure

The Spring Boot project (and the `frontend/` folder inside it) live in the `talenteval/` subdirectory of this repo:

```
talenteval/                  (this repo's root)
├── README.md                 you are here
├── PLAN.md                   feature-by-feature progress tracker — check this for current status
└── talenteval/                 the actual Spring Boot + React project
    ├── docs/                    full documentation (architecture, API, database, setup)
    ├── frontend/                React app
    ├── src/                     Spring Boot backend
    └── pom.xml
```

## Getting Started

See [talenteval/docs/setup.md](talenteval/docs/setup.md) for full setup instructions (prerequisites, environment variables, running backend + frontend, troubleshooting).

## Documentation

All project documentation lives in [talenteval/docs/](talenteval/docs/):

- [`architecture/high-level-architecture.md`](talenteval/docs/architecture/high-level-architecture.md) — system design, request flow, security layer
- [`database/schema.md`](talenteval/docs/database/schema.md) and [`er-diagram.md`](talenteval/docs/database/er-diagram.md) — data model
- [`api/`](talenteval/docs/api/) — one doc per feature's REST endpoints
- [`features/`](talenteval/docs/features/) — what each feature does and how it works end to end
- [`conventions.md`](talenteval/docs/conventions.md) — coding conventions used throughout the codebase
- [`setup.md`](talenteval/docs/setup.md) — how to run the project locally

## Project Status

This project is built and tracked incrementally, one feature at a time (backend fully done, then frontend, before moving to the next). **[PLAN.md](PLAN.md)** is the source of truth for what's done and what's next.
