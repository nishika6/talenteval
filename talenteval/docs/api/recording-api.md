# Recording API — TalentEval

Base URL: `http://localhost:8080/api/sessions/{sessionId}/recordings`

All endpoints require a valid JWT in the `Authorization: Bearer <token>` header.

Audio files are stored in Cloudinary, not in this backend or its database directly — these endpoints never expose a Cloudinary URL to the frontend. Uploads go through this API, and playback is served through an authenticated proxy endpoint that fetches the bytes from Cloudinary server-side (see [voice-recording.md](../features/voice-recording.md)).

---

## POST /api/sessions/{sessionId}/recordings

Upload a recorded answer for one question in the session.

**Role required:** CANDIDATE only, and must be the session's candidate

**Request:** `multipart/form-data`

| Field | Type | Description |
|---|---|---|
| questionId | Long | The question this recording answers — must be one of the questions added to the session |
| file | File | The recorded audio, e.g. `audio/webm` from the browser's `MediaRecorder` |

Uploading again for the same `questionId` overwrites the previous recording (re-recording is allowed until the session is completed).

**Success response (200 OK):**

```json
{
  "questionId": 1,
  "url": "/sessions/7/recordings/1/audio"
}
```

**Error responses (400 Bad Request):**

*Session doesn't exist:*
```json
{
  "error": "Session not found"
}
```

*Caller isn't this session's candidate:*
```json
{
  "error": "You are not the candidate of this session"
}
```

*Question wasn't added to this session:*
```json
{
  "error": "Question is not part of this session"
}
```

---

## GET /api/sessions/{sessionId}/recordings

List all recordings uploaded so far for a session.

**Role required:** INTERVIEWER or CANDIDATE (must be a participant in the session)

**Example request:**
```
GET /api/sessions/7/recordings
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
[
  { "questionId": 1, "url": "/sessions/7/recordings/1/audio" },
  { "questionId": 2, "url": "/sessions/7/recordings/2/audio" }
]
```

**Error response (400 Bad Request):**
```json
{
  "error": "You are not a participant of this session"
}
```

---

## GET /api/sessions/{sessionId}/recordings/{questionId}/audio

Stream the audio for one question's recording.

**Role required:** INTERVIEWER or CANDIDATE (must be a participant in the session)

Returns the raw audio bytes with `Content-Type: audio/webm` — this is the URL used directly in an `<audio>` element's `src`. The backend fetches the bytes from Cloudinary on each request; the frontend never talks to Cloudinary directly.

**Example request:**
```
GET /api/sessions/7/recordings/1/audio
Authorization: Bearer <token>
```

**Success response (200 OK):** binary audio data.

**Error response (400 Bad Request):**
```json
{
  "error": "Recording not found for this question"
}
```
