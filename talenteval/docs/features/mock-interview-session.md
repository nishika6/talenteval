# Feature: Mock Interview Session

## What It Does

Enables interviewers to conduct structured mock interview sessions with candidates. The interviewer starts a session, selects a candidate, picks questions from the question bank, and both participants go through the questions one by one in a guided flow. The session is saved with the date, participants, and all questions used.

## How It Works End to End

### Starting a Session

1. Interviewer navigates to the **Sessions** page and clicks "Start New Session."
2. React fetches the candidate list via `GET /api/users/candidates` and shows it. The interviewer selects one.
3. React sends a `POST /api/sessions` request with the candidate's ID.
4. The backend creates a new session record with the current interviewer, selected candidate, current timestamp, and status `IN_PROGRESS`.
5. The interviewer is taken to the session setup screen.

### Picking Questions

1. On the session setup screen, the interviewer sees the full question bank with filters (role, topic).
2. The interviewer selects questions they want to use in this session.
3. React sends a `POST /api/sessions/{id}/questions` request with the list of question IDs.
4. The backend creates `session_questions` records linking the selected questions to the session, preserving the order.
5. The questions appear in the session's question list.

### Guided Question-by-Question Flow

1. Once questions are selected, the interviewer starts the interview.
2. Both interviewer and candidate see the same question on screen, one at a time.
3. The interviewer can navigate forward and backward through the questions.
4. This flow is driven entirely on the frontend — no additional API calls are needed since all questions are already loaded.

### Completing a Session

1. After going through all questions, the interviewer clicks "Complete Session" (only shown on the last question).
2. React sends a `PUT /api/sessions/{id}/complete` request.
3. The backend changes the session status from `IN_PROGRESS` to `COMPLETED`.
4. A completed session cannot have questions added or removed.
5. The interviewer is taken directly to the scorecard form for this session (see [scorecard.md](../features/scorecard.md)).

### Candidate View

1. Candidates see all their sessions (past and active) on the same **Sessions** page, listed by interviewer name, date, and status.
2. Clicking a session shows the questions used and, if completed, the scorecard.
3. Candidates cannot start sessions, add questions, or mark sessions complete — the page renders read-only for them.

## Key Business Rules

- Only interviewers can start sessions, add questions, and complete sessions.
- A session must have exactly one interviewer and one candidate.
- The candidate must be a user with the CANDIDATE role.
- Questions can only be added to sessions with `IN_PROGRESS` status.
- Once a session is marked `COMPLETED`, it cannot be modified.
- The order of questions within a session is preserved.
- A session must be completed before a scorecard can be submitted for it.
