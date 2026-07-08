# Feature: Email Notifications

## What It Does

Keeps both participants informed by email at the two key handoff points in a session's lifecycle: when a candidate is assigned a new session, and when a candidate completes one. This is also what enables the asynchronous flow described in [mock-interview-session.md](mock-interview-session.md) — the candidate doesn't need to be online at the same time as the interviewer; they get an email whenever there's something for them to do.

## How It Works End to End

### Session Assigned (to Candidate)

1. An interviewer creates a session (`POST /api/sessions`), optionally with a scheduled date/time.
2. `SessionService.createSession()` calls `EmailService.notifyCandidate()` after saving the session.
3. The email includes the interviewer's name, the scheduled time if one was set, and a link to the site (`{frontend-url}/login`).
4. Sending happens on a background thread (`@Async`) — the API response to the interviewer's browser doesn't wait for the SMTP call to finish.

### Session Completed (to Interviewer)

1. A candidate finishes recording all their answers and completes the session (`PUT /api/sessions/{id}/complete`).
2. `SessionService.completeSession()` calls `EmailService.notifyInterviewer()`, but only when the caller completing it is the candidate — an interviewer completing their own session does not trigger this email (they already know).
3. The email includes the candidate's name and a link to the site.

### Password Reset

`EmailService.sendPasswordResetEmail()` is also part of this same service, used by the Forgot Password flow — see [authentication.md](authentication.md).

## Key Architecture Notes

- Built on `spring-boot-starter-mail` + `JavaMailSender`, SMTP configured entirely via environment variables (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`) — no credentials in `application.properties`.
- `@EnableAsync` on `TalentevalApplication`, `EmailService`'s methods marked `@Async`, so none of these ever block the request that triggered them.
- Emails are plain text (`SimpleMailMessage`), not HTML templates.
- If SMTP fails, the exception is only logged by Spring's async error handling — it does not surface back to the original caller, since that request has already returned successfully.
- Every email includes a link back to the site, built from `app.frontend-url` (env var `FRONTEND_URL`, defaults to `http://localhost:5173`).

## Key Business Rules

- The candidate is emailed once, when a session is created for them (not on every subsequent change to the session).
- The interviewer is emailed once, only when the candidate (not the interviewer) is the one who completes the session.
- Neither email is required for the app to function correctly — if SMTP is misconfigured, sessions and completions still succeed; only the notification silently fails.
