# Session API — TalentEval

Base URL: `http://localhost:8080/api/sessions`

All endpoints require a valid JWT in the `Authorization: Bearer <token>` header.

---

## POST /api/sessions

Start a new mock interview session.

**Role required:** INTERVIEWER only

**Request body:**

```json
{
  "candidateId": 3
}
```

| Field | Type | Validation | Description |
|---|---|---|---|
| candidateId | Long | Required | The ID of the candidate to interview |

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
  "questions": []
}
```

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
    "status": "COMPLETED",
    "questions": [
      {
        "id": 1,
        "title": "Design a URL shortening service",
        "role": "ENGINEERING",
        "topic": "System Design",
        "difficulty": "HARD",
        "questionOrder": 1
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
  "status": "IN_PROGRESS",
  "questions": [
    {
      "id": 1,
      "title": "Design a URL shortening service",
      "role": "ENGINEERING",
      "topic": "System Design",
      "difficulty": "HARD",
      "questionOrder": 1
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
      "questionOrder": 1
    },
    {
      "id": 5,
      "title": "What is your greatest strength?",
      "role": "HR",
      "topic": "Behavioral",
      "difficulty": "EASY",
      "questionOrder": 2
    },
    {
      "id": 12,
      "title": "Walk me through a product launch plan",
      "role": "PM",
      "topic": "Product Strategy",
      "difficulty": "MEDIUM",
      "questionOrder": 3
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

**Role required:** INTERVIEWER only (must be the session's interviewer)

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
  "status": "COMPLETED",
  "questions": [...]
}
```

**Error response (400 Bad Request):**
```json
{
  "error": "Session is already completed"
}
```
