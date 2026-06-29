# Auth API — TalentEval

Base URL: `http://localhost:8080/api/auth`

All auth endpoints are **public** — no JWT required.

---

## POST /api/auth/register

Register a new user account.

**Role required:** None (public)

**Request body:**

```json
{
  "name": "Priya Sharma",
  "email": "priya@example.com",
  "password": "securepass123",
  "role": "CANDIDATE"
}
```

| Field | Type | Validation | Description |
|---|---|---|---|
| name | String | Required, not blank | Full name |
| email | String | Required, valid email format | Login email |
| password | String | Required, min 6 characters | Account password |
| role | String | Required, must be `CANDIDATE` or `INTERVIEWER` | User role |

**Success response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwcml5YUBleGFtcGxlLmNvbSIsInJvbGUiOiJDQU5ESURBVEUiLCJpYXQiOjE3MTk0MDAwMDAsImV4cCI6MTcxOTQ4NjQwMH0.abc123",
  "name": "Priya Sharma",
  "email": "priya@example.com",
  "role": "CANDIDATE"
}
```

**Error responses:**

*Email already taken (400 Bad Request):*
```json
{
  "error": "Email already registered"
}
```

*Validation error (400 Bad Request):*
```json
{
  "email": "Invalid email format",
  "password": "Password must be at least 6 characters"
}
```

---

## POST /api/auth/login

Authenticate an existing user.

**Role required:** None (public)

**Request body:**

```json
{
  "email": "priya@example.com",
  "password": "securepass123"
}
```

| Field | Type | Validation | Description |
|---|---|---|---|
| email | String | Required, valid email format | Registered email |
| password | String | Required, not blank | Account password |

**Success response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwcml5YUBleGFtcGxlLmNvbSIsInJvbGUiOiJDQU5ESURBVEUiLCJpYXQiOjE3MTk0MDAwMDAsImV4cCI6MTcxOTQ4NjQwMH0.abc123",
  "name": "Priya Sharma",
  "email": "priya@example.com",
  "role": "CANDIDATE"
}
```

**Error responses:**

*Invalid credentials (401 Unauthorized):*
```json
{
  "error": "Invalid email or password"
}
```

*Validation error (400 Bad Request):*
```json
{
  "email": "Email is required"
}
```
