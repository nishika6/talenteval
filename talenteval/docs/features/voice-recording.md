# Feature: Voice Recording

## What It Does

Lets a candidate record a spoken answer for each question in their session, using the browser's microphone, instead of (or alongside) reading the questions silently. A recording is required for every question before the candidate can complete the session. Recordings are stored in Cloudinary and streamed back through an authenticated proxy endpoint so the interviewer can listen to each answer before filling out the scorecard.

## How It Works End to End

### Recording an Answer (Candidate)

1. On each question of their session, the candidate sees Start/Stop Recording controls.
2. Clicking "Start Recording" calls `navigator.mediaDevices.getUserMedia({ audio: true })` for microphone access and starts the browser's `MediaRecorder` API, capturing `audio/webm` chunks.
3. Clicking "Stop Recording" finalizes the blob and uploads it immediately as `multipart/form-data` to `POST /api/sessions/{sessionId}/recordings` with the `questionId` and the audio file (see [recording-api.md](../api/recording-api.md)).
4. The candidate sees a "Recorded ✓" badge once the upload succeeds, and can re-record (uploading again overwrites the previous recording for that question) any time before completing the session.

### Storing a Recording (Backend)

1. `RecordingService.uploadRecording()` verifies the caller is this session's candidate and that the question is actually part of the session.
2. The audio bytes are handed to `RecordingStorageService.store()` — implemented by `CloudinaryRecordingStorageService`, which uploads to Cloudinary with `resource_type: "video"` (Cloudinary has no dedicated audio resource type) under `talenteval/recordings/{sessionId}_{questionId}`.
3. Cloudinary returns a `secure_url`, which is saved on a `SessionRecording` row (creating it on first upload, updating it on re-record) — one row per `(session, question)` pair.

### Blocking Session Completion

1. When a candidate tries to complete their session (`PUT /api/sessions/{id}/complete`), `SessionService.completeSession()` calls `RecordingService.isFullyRecorded(session)`.
2. This compares the number of questions in the session to the number of recordings uploaded for it — if they don't match, the request fails with a 400 error ("Please record all questions before completing the session") and the "Complete Session" button stays disabled on the frontend until every question shows "Recorded ✓".
3. This check does not apply to an interviewer completing a session — only to a candidate.

### Playback (Interviewer)

1. On the session review page, before filling the scorecard, the interviewer sees an `<audio controls>` player per question.
2. Its source is `GET /api/sessions/{sessionId}/recordings/{questionId}/audio` — the frontend never talks to Cloudinary directly.
3. `RecordingService.getAudio()` re-checks that the caller is a participant in the session, then calls `RecordingStorageService.load()`, which fetches the bytes from the stored Cloudinary `secure_url` via `java.net.http.HttpClient` and streams them back with `Content-Type: audio/webm`.
4. This proxy pattern preserves the same "only session participants can access" authorization model as the rest of the API — a Cloudinary URL alone is never enough to fetch someone else's recording, since Cloudinary itself doesn't check who's asking.

## Key Architecture Notes

- `RecordingStorageService` is a small interface (`store()` / `load()`) specifically so the storage backend can be swapped without touching the entity, controller, or frontend. It originally had a `LocalRecordingStorageService` implementation (saved files under `uploads/recordings/` on disk) — confirmed working, then replaced with `CloudinaryRecordingStorageService` after review flagged that local files risk data loss (if the folder is ever deleted) and don't scale across multiple concurrent users on different machines. The local implementation has since been deleted from the codebase.
- `SessionRecording.filePath` stores the Cloudinary `secure_url`, not a filesystem path.
- Upload size is capped by `spring.servlet.multipart.max-file-size` / `max-request-size` (25MB each) in `application.properties`.

## Key Business Rules

- Only the session's candidate can upload a recording, and only for a question that's actually part of that session.
- One recording per `(session, question)` — re-uploading for the same question replaces it rather than creating a duplicate.
- A candidate cannot complete their session until every question in it has a recording.
- This requirement does not apply to an interviewer completing a session.
- Both the interviewer and the candidate (as participants) can list recordings and stream audio; no one else can.
