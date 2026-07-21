# Question Bank API — TalentEval

Base URL: `http://localhost:8080/api/questions`

All endpoints require a valid JWT in the `Authorization: Bearer <token>` header.

---

## GET /api/questions

Retrieve all questions. Supports optional filtering by role and topic.

**Role required:** INTERVIEWER or CANDIDATE

**Query parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| role | String | No | Filter by role: HR, UX, PM, FINANCE, ENGINEERING |
| topic | String | No | Filter by topic (partial match) |

**Example request:**
```
GET /api/questions?role=ENGINEERING&topic=System Design
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
[
  {
    "id": 1,
    "title": "Design a URL shortening service like bit.ly",
    "role": "ENGINEERING",
    "topic": "System Design",
    "difficulty": "HARD",
    "timeLimit": 180
  },
  {
    "id": 2,
    "title": "Explain the difference between TCP and UDP",
    "role": "ENGINEERING",
    "topic": "System Design",
    "difficulty": "EASY",
    "timeLimit": 120
  }
]
```

---

## GET /api/questions/{id}

Retrieve a single question by ID.

**Role required:** INTERVIEWER or CANDIDATE

**Example request:**
```
GET /api/questions/1
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
{
  "id": 1,
  "title": "Design a URL shortening service like bit.ly",
  "role": "ENGINEERING",
  "topic": "System Design",
  "difficulty": "HARD",
  "timeLimit": 180
}
```

**Error response (400 Bad Request):**
```json
{
  "error": "Question not found"
}
```

---

## POST /api/questions

Create a new question.

**Role required:** INTERVIEWER only

**Request body:**

```json
{
  "title": "How would you handle a conflict between two team members?",
  "role": "HR",
  "topic": "Conflict Resolution",
  "difficulty": "MEDIUM",
  "timeLimit": 150
}
```

| Field | Type | Validation | Description |
|---|---|---|---|
| title | String | Required, not blank | The question text |
| role | String | Required, must be HR / UX / PM / FINANCE / ENGINEERING | Target interview role |
| topic | String | Required, not blank | Topic category |
| difficulty | String | Required, must be EASY / MEDIUM / HARD | Difficulty level |
| timeLimit | Integer | Optional, 10–1800 seconds. Defaults to 120 if omitted | How long the candidate has to record an answer before it auto-stops |

**Success response (200 OK):**

```json
{
  "id": 15,
  "title": "How would you handle a conflict between two team members?",
  "role": "HR",
  "topic": "Conflict Resolution",
  "difficulty": "MEDIUM",
  "timeLimit": 150
}
```

**Error response (400 Bad Request):**
```json
{
  "title": "Title is required",
  "role": "Role must be HR, UX, PM, FINANCE, or ENGINEERING"
}
```

---

## PUT /api/questions/{id}

Update an existing question.

**Role required:** INTERVIEWER only

**Request body:**

```json
{
  "title": "How would you resolve a conflict between two team members?",
  "role": "HR",
  "topic": "Conflict Resolution",
  "difficulty": "HARD",
  "timeLimit": 180
}
```

**Success response (200 OK):**

```json
{
  "id": 15,
  "title": "How would you resolve a conflict between two team members?",
  "role": "HR",
  "topic": "Conflict Resolution",
  "difficulty": "HARD",
  "timeLimit": 180
}
```

**Error response (400 Bad Request):**
```json
{
  "error": "Question not found"
}
```

---

## DELETE /api/questions/{id}

Delete a question from the bank.

**Role required:** INTERVIEWER only

**Example request:**
```
DELETE /api/questions/15
Authorization: Bearer <token>
```

**Success response (200 OK):**

```json
{
  "message": "Question deleted successfully"
}
```

**Error response (400 Bad Request):**
```json
{
  "error": "Question not found"
}
```
