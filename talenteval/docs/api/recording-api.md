# Recording API — TalentEval

Base URL: `http://localhost:8080/api/sessions/{sessionId}/recordings`

All endpoints require a valid JWT in the `Authorization: Bearer <token>` header.

Video files are stored in Cloudinary, not in this backend or its database directly — these endpoints never expose a Cloudinary URL to the frontend. Uploads go through this API, and playback is served through an authenticated proxy endpoint that fetches the bytes from Cloudinary server-side (see [video-recording.md](../features/video-recording.md)).

---

## POST /api/sessions/{sessionId}/recordings

Upload a recorded answer for one question in the session.

**Role required:** CANDIDATE only, and must be the session's candidate

**Request:** `multipart/form-data`

| Field | Type | Description |
|---|---|---|
| questionId | Long | The question this recording answers — must be one of the questions added to the session |
| file | File | The recorded video, e.g. `video/webm` from the browser's `MediaRecorder` (captured with both `video: true` and `audio: true`) |

Uploading again for the same `questionId` overwrites the previous recording (re-recording is allowed until the session is completed).

**Success response (200 OK):**

```json
{
  "questionId": 1,
  "url": "/sessions/7/recordings/1/video"
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
  { "questionId": 1, "url": "/sessions/7/recordings/1/video" },
  { "questionId": 2, "url": "/sessions/7/recordings/2/video" }
]
```

**Error response (400 Bad Request):**
```json
{
  "error": "You are not a participant of this session"
}
```

---

## GET /api/sessions/{sessionId}/recordings/{questionId}/video

Stream the video for one question's recording.

**Role required:** INTERVIEWER or CANDIDATE (must be a participant in the session)

Returns the raw video bytes with `Content-Type: video/webm` — this is the URL used directly in a `<video>` element's `src`. The backend fetches the bytes from Cloudinary on each request; the frontend never talks to Cloudinary directly.

**Example request:**
```
GET /api/sessions/7/recordings/1/video
Authorization: Bearer <token>
```

**Success response (200 OK):** binary video data.

**Error response (400 Bad Request):**
```json
{
  "error": "Recording not found for this question"
}
```
