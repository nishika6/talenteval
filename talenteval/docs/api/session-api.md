# Session API — TalentEval

Base URL: `http://localhost:8080/api/sessions`

All endpoints require a valid JWT in the `Authorization: Bearer <token>` header.

---

## GET /api/users/candidates

Get all registered candidates, used by the interviewer to pick who to interview when starting a session.

**Role required:** INTERVIEWER only

**Example request:**
```
GET /api/users/candidates
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
[
  {
    "id": 3,
    "name": "Priya Sharma",
    "email": "priya@example.com",
    "role": "CANDIDATE"
  },
  {
    "id": 4,
    "name": "Arjun Mehta",
    "email": "arjun@example.com",
    "role": "CANDIDATE"
  }
]
```

---

## POST /api/sessions

Start a new mock interview session.

**Role required:** INTERVIEWER only

**Request body:**

```json
{
  "candidateId": 3,
  "scheduledAt": "2026-07-15T14:00:00"
}
```

| Field | Type | Validation | Description |
|---|---|---|---|
| candidateId | Long | Required | The ID of the candidate to interview |
| scheduledAt | LocalDateTime | Optional | When the session is scheduled for. If omitted, the session has no scheduled time and only the creation timestamp (`date`) is shown |

**Success response (200 OK):**

```json
{
  "id": 1,
  "interviewerId": 2,
  "interviewerName": "Rahul Verma",
  "candidateId": 3,
  "candidateName": "Priya Sharma",
  "date": "2026-06-29T10:30:00",
  "scheduledAt": "2026-07-15T14:00:00",
  "status": "IN_PROGRESS",
  "questions": []
}
```

Creating a session emails the candidate (see [email-notifications.md](../features/email-notifications.md)) — the email includes the scheduled time if one was set.

**Error responses:**

*Candidate not found (400 Bad Request):*
```json
{
  "error": "Candidate not found"
}
```

*User is not a candidate (400 Bad Request):*
```json
{
  "error": "Selected user is not a candidate"
}
```

---

## GET /api/sessions

Get all sessions for the current user.

**Role required:** INTERVIEWER or CANDIDATE

- Interviewers see all sessions they have conducted.
- Candidates see all sessions they have participated in.

**Example request:**
```
GET /api/sessions
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
[
  {
    "id": 1,
    "interviewerId": 2,
    "interviewerName": "Rahul Verma",
    "candidateId": 3,
    "candidateName": "Priya Sharma",
    "date": "2026-06-29T10:30:00",
    "scheduledAt": null,
    "status": "COMPLETED",
    "questions": [
      {
        "id": 1,
        "title": "Design a URL shortening service",
        "role": "ENGINEERING",
        "topic": "System Design",
        "difficulty": "HARD",
        "questionOrder": 1,
        "timeLimit": 180
      }
    ]
  }
]
```

---

## GET /api/sessions/{id}

Get a specific session by ID.

**Role required:** INTERVIEWER or CANDIDATE (must be a participant)

**Success response (200 OK):**

```json
{
  "id": 1,
  "interviewerId": 2,
  "interviewerName": "Rahul Verma",
  "candidateId": 3,
  "candidateName": "Priya Sharma",
  "date": "2026-06-29T10:30:00",
  "scheduledAt": "2026-07-15T14:00:00",
  "status": "IN_PROGRESS",
  "questions": [
    {
      "id": 1,
      "title": "Design a URL shortening service",
      "role": "ENGINEERING",
      "topic": "System Design",
      "difficulty": "HARD",
      "questionOrder": 1,
      "timeLimit": 180
    }
  ]
}
```

---

## POST /api/sessions/{id}/questions

Add questions to an in-progress session.

**Role required:** INTERVIEWER only (must be the session's interviewer)

**Request body:**

```json
{
  "questionIds": [1, 5, 12]
}
```

| Field | Type | Validation | Description |
|---|---|---|---|
| questionIds | List of Long | Required, not empty | IDs of questions to add to the session |

**Success response (200 OK):**

```json
{
  "id": 1,
  "interviewerId": 2,
  "interviewerName": "Rahul Verma",
  "candidateId": 3,
  "candidateName": "Priya Sharma",
  "date": "2026-06-29T10:30:00",
  "status": "IN_PROGRESS",
  "questions": [
    {
      "id": 1,
      "title": "Design a URL shortening service",
      "role": "ENGINEERING",
      "topic": "System Design",
      "difficulty": "HARD",
      "questionOrder": 1,
      "timeLimit": 180
    },
    {
      "id": 5,
      "title": "What is your greatest strength?",
      "role": "HR",
      "topic": "Behavioral",
      "difficulty": "EASY",
      "questionOrder": 2,
      "timeLimit": 120
    },
    {
      "id": 12,
      "title": "Walk me through a product launch plan",
      "role": "PM",
      "topic": "Product Strategy",
      "difficulty": "MEDIUM",
      "questionOrder": 3,
      "timeLimit": 120
    }
  ]
}
```

**Error response (400 Bad Request):**
```json
{
  "error": "Session is already completed"
}
```

---

## PUT /api/sessions/{id}/complete

Mark a session as completed.

**Role required:** INTERVIEWER or CANDIDATE — either participant can complete the session (this endpoint has no `@PreAuthorize`; `SessionService` checks the caller's role and their relationship to the session internally).

- If the **interviewer** completes it, they're taken straight to the scorecard form.
- If the **candidate** completes it, they're returned to the session list, and the interviewer is emailed (see [email-notifications.md](../features/email-notifications.md)).
- A candidate must have recorded an answer for every question in the session before they're allowed to complete it (see [video-recording.md](../features/video-recording.md)) — this restriction does not apply to the interviewer.

**Example request:**
```
PUT /api/sessions/1/complete
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
{
  "id": 1,
  "interviewerId": 2,
  "interviewerName": "Rahul Verma",
  "candidateId": 3,
  "candidateName": "Priya Sharma",
  "date": "2026-06-29T10:30:00",
  "scheduledAt": "2026-07-15T14:00:00",
  "status": "COMPLETED",
  "questions": [...]
}
```

**Error responses (400 Bad Request):**

*Session already completed:*
```json
{
  "error": "Session is already completed"
}
```

*Candidate hasn't recorded every question yet:*
```json
{
  "error": "Please record all questions before completing the session"
}
```

*Caller isn't a participant of this session:*
```json
{
  "error": "You are not the candidate of this session"
}
```
