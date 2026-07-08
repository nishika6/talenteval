# Feature: Scheduler

## What It Does

Lets an interviewer optionally set a date and time when creating a session, so both participants know when the interview is meant to happen. This is separate from the session's creation timestamp — a session can be created today but scheduled for next week.

## How It Works End to End

1. When starting a new session, the interviewer optionally picks a date & time using a `datetime-local` input, alongside selecting the candidate.
2. React includes `scheduledAt` in the `POST /api/sessions` request body if a value was picked (see [session-api.md](../api/session-api.md)).
3. The backend stores it on the `InterviewSession` entity as a nullable `scheduledAt` field, separate from `date` (the creation timestamp, always set automatically).
4. If a `scheduledAt` was set, it's included in the email sent to the candidate (see [email-notifications.md](email-notifications.md)).
5. Session list cards and the session detail view show the scheduled time if one was set, falling back to the creation time (`date`) otherwise.
6. The Dashboard's "Upcoming Sessions" section (see [progress-dashboard.md](progress-dashboard.md)) is driven entirely by whether `scheduledAt` is set and the session isn't yet completed.

## Key Business Rules

- `scheduledAt` is optional — a session can exist with no scheduled time at all.
- Only the interviewer sets `scheduledAt`, and only at session creation — there's no separate "reschedule" endpoint.
- `scheduledAt` has no effect on session behavior (a session doesn't automatically lock or unlock based on the scheduled time) — it's informational and used for display/sorting only.
