# Feature: Mock Interview Session

## What It Does

Enables interviewers to conduct structured mock interview sessions with candidates, asynchronously. The interviewer starts a session, selects a candidate, optionally schedules a date/time, and picks questions from the question bank. The candidate then works through the questions independently in their own guided view — recording a spoken answer for each one — at whatever time suits them, rather than live alongside the interviewer. The interviewer reviews the recordings and fills a scorecard afterward. The session is saved with the date, participants, and all questions used.

## How It Works End to End

### Starting a Session

1. Interviewer navigates to the **Sessions** page and clicks "Start New Session."
2. React fetches the candidate list via `GET /api/users/candidates` and shows it. The interviewer selects one, and optionally picks a date & time via a `datetime-local` input (see [scheduler.md](scheduler.md)).
3. React sends a `POST /api/sessions` request with the candidate's ID and optional `scheduledAt`.
4. The backend creates a new session record with the current interviewer, selected candidate, current timestamp, optional scheduled time, and status `IN_PROGRESS`, then emails the candidate that they've been assigned a session (see [email-notifications.md](email-notifications.md)).
5. The interviewer is taken to the session setup screen.

### Picking Questions

1. On the session setup screen, the interviewer sees the full question bank with filters (role, topic).
2. The interviewer selects questions they want to use in this session.
3. React sends a `POST /api/sessions/{id}/questions` request with the list of question IDs.
4. The backend creates `session_questions` records linking the selected questions to the session, preserving the order.
5. The questions appear in the session's question list.

### Guided Question-by-Question Flow

1. Once questions are selected, each participant works through them independently in their own guided, one-at-a-time view (not a shared live screen) — the interviewer can navigate forward/backward through questions when picking them or reviewing later; the candidate does the same when recording answers.
2. This flow is driven entirely on the frontend — no additional API calls are needed since all questions are already loaded, aside from the candidate's per-question recording uploads (see [voice-recording.md](voice-recording.md)).

### Completing a Session

Either participant can complete a session, and each has a different flow:

**Interviewer completes it:**
1. After going through all questions, the interviewer clicks "Complete Session" (only shown on the last question).
2. React sends a `PUT /api/sessions/{id}/complete` request.
3. The backend changes the session status from `IN_PROGRESS` to `COMPLETED`.
4. The interviewer is taken directly to the scorecard form for this session (see [scorecard.md](scorecard.md)).

**Candidate completes it (async flow):**
1. The candidate records an answer for every question in the session first (see [voice-recording.md](voice-recording.md)) — completion is blocked with a 400 error until every question has a recording.
2. The candidate clicks "Complete Session" on the last question.
3. React sends the same `PUT /api/sessions/{id}/complete` request.
4. The backend marks the session `COMPLETED` and emails the interviewer that the candidate has finished (see [email-notifications.md](email-notifications.md)).
5. The candidate is returned to the session list; the interviewer reviews the recordings and fills the scorecard afterward, whenever they get to it.

A completed session cannot have questions added or removed, and cannot be completed a second time.

### Candidate View

1. Candidates see all their sessions (past and active) on the same **Sessions** page, listed by interviewer name, date/scheduled time, and status.
2. Clicking a session shows the questions used, lets the candidate record an answer per question, and — if completed — shows the scorecard.
3. Candidates cannot start sessions or add questions — only interviewers do that. Candidates can complete their own session, subject to the recording requirement above.

## Key Business Rules

- Only interviewers can start sessions and add questions.
- Either the interviewer or the candidate can mark a session complete — the interviewer must own it, the candidate must be its participant, and a candidate must have recorded every question first.
- A session must have exactly one interviewer and one candidate.
- The candidate must be a user with the CANDIDATE role.
- Questions can only be added to sessions with `IN_PROGRESS` status.
- Once a session is marked `COMPLETED`, it cannot be modified.
- The order of questions within a session is preserved.
- A session must be completed before a scorecard can be submitted for it.
