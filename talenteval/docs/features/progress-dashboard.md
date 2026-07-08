# Feature: Progress Dashboard

## What It Does

Gives candidates a comprehensive view of their interview performance over time. The dashboard shows all past sessions with their scores across the four criteria (Communication, Structure, Content, Confidence), overall averages, a visual representation of improvement over time, and the interviewer's written comments on each session. Interviewers can also view any candidate's progress history. The dashboard also surfaces upcoming (scheduled, not-yet-completed) sessions for both roles.

## How It Works End to End

### Candidate View

1. Candidate navigates to the **Dashboard** page.
2. React sends a `GET /api/progress/me` request.
3. The backend fetches all completed sessions for the logged-in candidate, along with their scorecards.
4. It calculates the overall average for each criterion across all sessions.
5. Returns the data sorted by session date (oldest first) to show progression.
6. The frontend renders:
   - **Summary cards** showing the total number of sessions and average score for each criterion.
   - **Session history table** listing each session with its date, interviewer name, individual scores, and the interviewer's written comments (shown only when present).
   - **Improvement view** showing how scores have changed across sessions over time.

### Interviewer View

1. Interviewer navigates to a candidate's profile or the progress view.
2. React sends a `GET /api/progress/candidate/{candidateId}` request.
3. The backend returns the same progress data as the candidate view.
4. The interviewer can see the candidate's full history across all sessions (including sessions conducted by other interviewers).

### Data Aggregation

The backend aggregates the following for the progress response:
- Total number of completed sessions with scorecards.
- Average score for each of the four criteria across all sessions.
- Per-session breakdown with date, interviewer name, individual criterion scores, and the interviewer's comments (if any).

### Upcoming Sessions

This part of the dashboard has no dedicated backend endpoint — it's derived entirely on the frontend from the existing `GET /api/sessions` response (see [session-api.md](../api/session-api.md)), filtering for sessions that have a `scheduledAt` and aren't yet `COMPLETED`.

- **Candidate view:** an "Upcoming Sessions" section lists each one with the interviewer's name, scheduled date/time, and a "Go to Session" button.
- **Interviewer view:** two separate sections — "Upcoming Sessions" and the 5 most "Recently Completed" — each with candidate name, date/time, a status badge, and a "View Session" button. These are kept as two distinct lists (not one merged/sorted list) so a "COMPLETED" badge never appears under the "Upcoming" heading.
- Clicking a session's button navigates to `/sessions`, passing the target session's id via React Router `state`; the Sessions page reads it on mount and auto-opens that session.

## Key Business Rules

- Only candidates can access the `/api/progress/me` endpoint.
- Interviewers can view any candidate's progress via `/api/progress/candidate/{candidateId}`.
- Candidates cannot view other candidates' progress.
- Only completed sessions with submitted scorecards are included in progress data.
- Sessions are displayed in chronological order (oldest first) to show improvement trajectory.
- Average scores are calculated across all sessions (not weighted by recency).
- If a candidate has no completed sessions with scorecards, the dashboard shows an empty state with a message encouraging them to participate in interviews.
