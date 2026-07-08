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

The email is normalized before it's checked or stored: lowercased, and anything from `+` onward in the local part is stripped (e.g. `Nishika+test@Gmail.com` → `nishika@gmail.com`). This prevents registering a second account for the same inbox via a `+alias`.

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

The email is normalized the same way as registration before lookup, so logging in with a `+alias` variant of the email used at registration still resolves to the same account.

---

## POST /api/auth/forgot-password

Request a password reset link.

**Role required:** None (public)

**Request body:**

```json
{
  "email": "priya@example.com"
}
```

| Field | Type | Validation | Description |
|---|---|---|---|
| email | String | Required, valid email format | The account's email |

**Success response (200 OK)** — always the same response, whether or not the email is registered (prevents an attacker from using this endpoint to discover which emails have accounts):

```json
{
  "message": "If that email is registered, a reset link has been sent."
}
```

If the email does match a user, a reset link (`{frontend-url}/reset-password?token=...`) is emailed to them. The token is single-use and expires after 30 minutes.

---

## POST /api/auth/reset-password

Set a new password using a reset token from the email link.

**Role required:** None (public)

**Request body:**

```json
{
  "token": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "newPassword": "newSecurePass456"
}
```

| Field | Type | Validation | Description |
|---|---|---|---|
| token | String | Required | The token from the reset link |
| newPassword | String | Required, min 6 characters | The new password |

**Success response (200 OK):**

```json
{
  "message": "Password reset successful."
}
```

**Error response (400 Bad Request)** — token missing, already used, or expired:
```json
{
  "error": "Invalid or expired reset link"
}
```
