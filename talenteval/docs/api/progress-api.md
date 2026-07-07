# Progress API — TalentEval

Base URL: `http://localhost:8080/api/progress`

All endpoints require a valid JWT in the `Authorization: Bearer <token>` header.

---

## GET /api/progress/me

Get the progress dashboard for the currently logged-in candidate.

**Role required:** CANDIDATE only

**Example request:**
```
GET /api/progress/me
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
{
  "candidateId": 3,
  "candidateName": "Priya Sharma",
  "totalSessions": 3,
  "averageScores": {
    "communication": 4.0,
    "structure": 3.7,
    "content": 4.3,
    "confidence": 4.0
  },
  "sessions": [
    {
      "sessionId": 1,
      "interviewerName": "Rahul Verma",
      "date": "2026-06-20T10:30:00",
      "communication": 3,
      "structure": 3,
      "content": 4,
      "confidence": 3
    },
    {
      "sessionId": 3,
      "interviewerName": "Rahul Verma",
      "date": "2026-06-25T14:00:00",
      "communication": 4,
      "structure": 4,
      "content": 4,
      "confidence": 4
    },
    {
      "sessionId": 5,
      "interviewerName": "Anita Desai",
      "date": "2026-06-29T09:00:00",
      "communication": 5,
      "structure": 4,
      "content": 5,
      "confidence": 5
    }
  ]
}
```

This response is designed for the frontend to render:
- Overall averages across all sessions
- Per-session scores showing improvement over time (sessions are ordered by date)

---

## GET /api/progress/candidate/{candidateId}

Get progress data for a specific candidate.

**Role required:** INTERVIEWER only

**Example request:**
```
GET /api/progress/candidate/3
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
{
  "candidateId": 3,
  "candidateName": "Priya Sharma",
  "totalSessions": 3,
  "averageScores": {
    "communication": 4.0,
    "structure": 3.7,
    "content": 4.3,
    "confidence": 4.0
  },
  "sessions": [
    {
      "sessionId": 1,
      "interviewerName": "Rahul Verma",
      "date": "2026-06-20T10:30:00",
      "communication": 3,
      "structure": 3,
      "content": 4,
      "confidence": 3
    },
    {
      "sessionId": 3,
      "interviewerName": "Rahul Verma",
      "date": "2026-06-25T14:00:00",
      "communication": 4,
      "structure": 4,
      "content": 4,
      "confidence": 4
    },
    {
      "sessionId": 5,
      "interviewerName": "Anita Desai",
      "date": "2026-06-29T09:00:00",
      "communication": 5,
      "structure": 4,
      "content": 5,
      "confidence": 5
    }
  ]
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
  "error": "User is not a candidate"
}
```
