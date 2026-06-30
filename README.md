# TalentEval

A role-agnostic mock interview and talent evaluation platform. An interviewer runs a live session with a candidate, picks questions from a question bank, fills a structured scorecard after the session, and the candidate tracks their progress over time. TalentEval is interviewer-led — it is not a self-serve test platform.

## Roles

- **INTERVIEWER** — starts sessions, picks questions, fills scorecards, views all candidate progress
- **CANDIDATE** — views their own past sessions, scores, and progress

## Features

1. **User Authentication** — email/password login, JWT sessions, BCrypt hashing, role selected at registration
2. **Question Bank** — questions tagged by role (HR/UX/PM/Finance/Engineering), topic, and difficulty; interviewers manage them, candidates browse read-only
3. **Mock Interview Session** — interviewer selects a candidate, picks questions, walks through them together in a guided flow
4. **Scorecard** — interviewer rates the candidate 1-5 on Communication, Structure, Content, and Confidence, with optional comments
5. **Progress Dashboard** — candidates (and interviewers, for any candidate) see score trends across sessions over time

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.5.15, Spring Security, JWT (JJWT), MySQL 8, Spring Data JPA / Hibernate
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
