# Scorecard API — TalentEval

Base URL: `http://localhost:8080/api/scorecards`

All endpoints require a valid JWT in the `Authorization: Bearer <token>` header.

---

## POST /api/scorecards

Submit a scorecard for a completed session.

**Role required:** INTERVIEWER only (must be the session's interviewer)

**Request body:**

```json
{
  "sessionId": 1,
  "communication": 4,
  "structure": 3,
  "content": 5,
  "confidence": 4,
  "comments": "Strong technical knowledge. Could improve on structuring answers more clearly. Overall a solid performance."
}
```

| Field | Type | Validation | Description |
|---|---|---|---|
| sessionId | Long | Required | The session being evaluated |
| communication | Integer | Required, 1-5 | Rating for communication skills |
| structure | Integer | Required, 1-5 | Rating for structured thinking |
| content | Integer | Required, 1-5 | Rating for content quality and accuracy |
| confidence | Integer | Required, 1-5 | Rating for confidence and composure |
| comments | String | Optional | Written feedback from the interviewer |

**Success response (200 OK):**

```json
{
  "id": 1,
  "sessionId": 1,
  "candidateId": 3,
  "candidateName": "Priya Sharma",
  "communication": 4,
  "structure": 3,
  "content": 5,
  "confidence": 4,
  "comments": "Strong technical knowledge. Could improve on structuring answers more clearly. Overall a solid performance.",
  "sessionDate": "2026-06-29T10:30:00"
}
```

**Error responses:**

*Session not completed (400 Bad Request):*
```json
{
  "error": "Cannot submit scorecard for an incomplete session"
}
```

*Scorecard already exists (400 Bad Request):*
```json
{
  "error": "A scorecard already exists for this session"
}
```

*Invalid rating (400 Bad Request):*
```json
{
  "communication": "Rating must be between 1 and 5"
}
```

---

## GET /api/scorecards/session/{sessionId}

Get the scorecard for a specific session.

**Role required:** INTERVIEWER or CANDIDATE (must be a participant in the session)

**Example request:**
```
GET /api/scorecards/session/1
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
{
  "id": 1,
  "sessionId": 1,
  "candidateId": 3,
  "candidateName": "Priya Sharma",
  "communication": 4,
  "structure": 3,
  "content": 5,
  "confidence": 4,
  "comments": "Strong technical knowledge. Could improve on structuring answers more clearly. Overall a solid performance.",
  "sessionDate": "2026-06-29T10:30:00"
}
```

**Error response (404 Not Found):**
```json
{
  "error": "Scorecard not found for this session"
}
```

---

## GET /api/scorecards/candidate/{candidateId}

Get all scorecards for a specific candidate.

**Role required:** INTERVIEWER (can view any candidate), or CANDIDATE (can only view their own)

**Example request:**
```
GET /api/scorecards/candidate/3
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
[
  {
    "id": 1,
    "sessionId": 1,
    "candidateId": 3,
    "candidateName": "Priya Sharma",
    "communication": 4,
    "structure": 3,
    "content": 5,
    "confidence": 4,
    "comments": "Strong technical knowledge.",
    "sessionDate": "2026-06-29T10:30:00"
  },
  {
    "id": 3,
    "sessionId": 5,
    "candidateId": 3,
    "candidateName": "Priya Sharma",
    "communication": 5,
    "structure": 4,
    "content": 5,
    "confidence": 5,
    "comments": "Excellent improvement since last session.",
    "sessionDate": "2026-07-05T14:00:00"
  }
]
```
