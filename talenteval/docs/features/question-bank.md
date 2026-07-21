# Feature: Question Bank

## What It Does

Provides a centralized bank of interview questions organized by role, topic, and difficulty, each with a time limit for recording an answer. Interviewers can create, edit, and delete questions. Candidates can browse and filter them but cannot modify them. The system comes pre-seeded with default questions across all five roles on first startup.

## How It Works End to End

### Interviewer: Adding a Question

1. Interviewer navigates to the **Question Bank** page.
2. Clicks "Add Question" and fills in the title, selects a role (HR / UX / PM / Finance / Engineering), enters a topic, picks a difficulty level (Easy / Medium / Hard), and optionally sets a time limit in seconds (defaults to 120 if left as-is).
3. React sends a `POST /api/questions` request with the question data.
4. The backend validates that the user has the INTERVIEWER role (via `@PreAuthorize`), validates the input fields, and saves the question to the `questions` table.
5. The new question appears in the list.

### Interviewer: Editing / Deleting a Question

1. On the Question Bank page, each question has Edit and Delete actions.
2. **Edit:** Opens the question form pre-filled with existing data. On save, React sends a `PUT /api/questions/{id}` request.
3. **Delete:** After confirmation, React sends a `DELETE /api/questions/{id}` request.
4. Both operations are restricted to INTERVIEWER role on the backend.

### Candidate: Browsing Questions

1. Candidate navigates to the **Question Bank** page.
2. Sees all questions in a read-only view (no Add/Edit/Delete buttons).
3. Can filter by role using a dropdown (e.g., show only Engineering questions).
4. Can filter by topic using a search field.
5. React sends `GET /api/questions?role=ENGINEERING&topic=System Design` to fetch filtered results.

### Pre-seeding Default Questions

1. On application startup, a database seeder checks if the `questions` table is empty.
2. If empty, it inserts a set of default questions covering all five roles and multiple topics.
3. This ensures the platform is usable immediately without manual data entry.

## Key Business Rules

- Only users with the INTERVIEWER role can create, update, or delete questions.
- Candidates have read-only access to the question bank.
- Every question must have a title, role, topic, and difficulty.
- Valid roles: HR, UX, PM, FINANCE, ENGINEERING.
- Valid difficulties: EASY, MEDIUM, HARD.
- Time limit is optional on the form; defaults to 120 seconds if not set, and must be between 10 and 1800 seconds if it is. This is the time a candidate gets to record an answer to this question (see [video-recording.md](video-recording.md)).
- Questions can be used across multiple interview sessions.
- Deleting a question does not affect past sessions that used it (the session retains a reference).
