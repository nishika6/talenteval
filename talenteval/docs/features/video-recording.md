# Feature: Video Recording

## What It Does

Lets a candidate record a video answer for each question in their session, using the browser's camera and microphone together, instead of reading the questions silently. Each question has a time limit (set in the Question Bank, see [question-bank.md](question-bank.md)) — a countdown starts as soon as recording begins, and recording auto-stops when it runs out. A recording is required for every question before the candidate can complete the session. Recordings are stored in Cloudinary and streamed back through an authenticated proxy endpoint so the interviewer can watch each answer before filling out the scorecard.

This feature replaced an earlier audio-only version of the same flow — see the note under Key Architecture Notes below.

## How It Works End to End

### Recording an Answer (Candidate)

1. On each question of their session, the candidate sees Start/Stop Recording controls.
2. Clicking "Start Recording" calls `navigator.mediaDevices.getUserMedia({ video: true, audio: true })` for camera and microphone access and starts the browser's `MediaRecorder` API, capturing `video/webm` chunks (video + audio muxed together). A live self-preview appears at the same time — a **muted** `<video>` element showing the candidate their own camera feed, so they can see themselves while recording without hearing their own mic as an echo. A countdown also starts at the same moment, counting down from the question's `timeLimit` (in seconds), shown next to the recording controls and turning red in the last 30 seconds.
3. Clicking "Stop Recording" finalizes the blob and uploads it immediately as `multipart/form-data` to `POST /api/sessions/{sessionId}/recordings` with the `questionId` and the video file (see [recording-api.md](../api/recording-api.md)). The countdown clears and the live preview disappears at the same time.
4. If the countdown reaches 0 before the candidate stops manually, recording auto-stops through the same path (triggering the same upload) and the candidate is advanced to the next question automatically — except on the last question, where it just stops/uploads and leaves the candidate there for the existing "Complete Session" action.
5. The candidate sees a "Recorded ✓" badge once the upload succeeds, and can re-record (uploading again overwrites the previous recording for that question) any time before completing the session.

### Storing a Recording (Backend)

1. `RecordingService.uploadRecording()` verifies the caller is this session's candidate and that the question is actually part of the session.
2. The video bytes are handed to `RecordingStorageService.store()` — implemented by `CloudinaryRecordingStorageService`, which uploads to Cloudinary with `resource_type: "video"` under `talenteval/recordings/{sessionId}_{questionId}`.
3. Cloudinary returns a `secure_url`, which is saved on a `SessionRecording` row (creating it on first upload, updating it on re-record) — one row per `(session, question)` pair. The column is still named `filePath` and holds a video URL now instead of an audio one — no schema change was needed for this, since it was always just a generic URL string.

### Blocking Session Completion

1. When a candidate tries to complete their session (`PUT /api/sessions/{id}/complete`), `SessionService.completeSession()` calls `RecordingService.isFullyRecorded(session)`.
2. This compares the number of questions in the session to the number of recordings uploaded for it — if they don't match, the request fails with a 400 error ("Please record all questions before completing the session") and the "Complete Session" button stays disabled on the frontend until every question shows "Recorded ✓".
3. This check does not apply to an interviewer completing a session — only to a candidate.

### Playback (Interviewer)

1. On the session review page, before filling the scorecard, the interviewer sees a `<video controls>` player per question.
2. Its source is `GET /api/sessions/{sessionId}/recordings/{questionId}/video` — the frontend never talks to Cloudinary directly.
3. `RecordingService.getVideo()` re-checks that the caller is a participant in the session, then calls `RecordingStorageService.load()`, which fetches the bytes from the stored Cloudinary `secure_url` via `java.net.http.HttpClient` and streams them back with `Content-Type: video/webm`.
4. This proxy pattern preserves the same "only session participants can access" authorization model as the rest of the API — a Cloudinary URL alone is never enough to fetch someone else's recording, since Cloudinary itself doesn't check who's asking.

## Key Architecture Notes

- **This feature replaced audio-only voice recording.** The original version captured `audio: true` only and played back through `<audio controls>`; upgrading to video meant adding `video: true` to `getUserMedia()`, switching the `MediaRecorder` and Cloudinary upload to `video/webm`, and swapping `<audio>` for `<video>` on the interviewer's side. Two things this did **not** require, worth knowing if asked: Cloudinary's upload call already used `resource_type: "video"` (audio was uploaded that way from the start, since Cloudinary has no dedicated audio type), and the database needed no migration, since `SessionRecording.filePath` was always just a generic URL column, agnostic to what kind of file it points at.
- `RecordingStorageService` is a small interface (`store()` / `load()`) specifically so the storage backend can be swapped without touching the entity, controller, or frontend. It originally had a `LocalRecordingStorageService` implementation (saved files under `uploads/recordings/` on disk) — confirmed working, then replaced with `CloudinaryRecordingStorageService` after review flagged that local files risk data loss (if the folder is ever deleted) and don't scale across multiple concurrent users on different machines. The local implementation has since been deleted from the codebase.
- The countdown's auto-stop/advance-to-next-question logic lives in a `useEffect` watching `timeLeft`, not inside the `setTimeLeft` updater function — `main.jsx` wraps the app in `<StrictMode>`, which can invoke updater functions more than once, which would risk double-uploading a recording or skipping two questions instead of one.
- The live self-preview is wired in its own `useEffect` watching `isRecording`, not set directly inside `startRecording()` — the `<video>` preview element is only mounted once `isRecording` becomes true, so trying to assign its `srcObject` synchronously inside `startRecording()` (before that state update commits) would silently do nothing on the very first recording.
- Upload size is capped by `spring.servlet.multipart.max-file-size` / `max-request-size` — raised from `25MB` to `100MB` when this feature shipped, since video at the same duration is significantly larger than audio-only.

## Key Business Rules

- Only the session's candidate can upload a recording, and only for a question that's actually part of that session.
- One recording per `(session, question)` — re-uploading for the same question replaces it rather than creating a duplicate.
- A candidate cannot complete their session until every question in it has a recording.
- This requirement does not apply to an interviewer completing a session.
- Both the interviewer and the candidate (as participants) can list recordings and stream video; no one else can.
