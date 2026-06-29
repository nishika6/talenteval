# Feature: User Authentication

## What It Does

Allows users to create an account and log in to TalentEval. Every user selects a role at registration — either **CANDIDATE** or **INTERVIEWER** — which determines what they can do in the system. Authentication is handled via JWT tokens, and passwords are securely hashed with BCrypt.

## How It Works End to End

### Registration Flow

1. User opens the app and navigates to the **Register** page.
2. User fills in their full name, email, password, and selects a role (Candidate or Interviewer).
3. React sends a `POST /api/auth/register` request to the backend.
4. The backend validates the input (email format, password length, valid role).
5. It checks if the email is already registered — if so, returns an error.
6. The password is hashed using BCrypt and the user is saved to the `users` table in MySQL.
7. A JWT token is generated containing the user's email and role, signed with a secret key.
8. The token, name, email, and role are returned to the frontend.
9. React stores the token in `localStorage` and redirects to the Dashboard.

### Login Flow

1. User navigates to the **Login** page.
2. User enters their email and password.
3. React sends a `POST /api/auth/login` request.
4. Spring Security's `AuthenticationManager` loads the user from the database (via `UserDetailsServiceImpl`) and compares the provided password against the stored BCrypt hash.
5. If credentials are valid, a new JWT is generated and returned.
6. React stores the token and redirects to the Dashboard.
7. If credentials are invalid, the backend returns a 401 error and the frontend displays "Invalid email or password."

### Authenticated Requests

1. On every subsequent API call, Axios automatically attaches the JWT as a `Bearer` token in the `Authorization` header (via a request interceptor).
2. The `JwtAuthenticationFilter` on the backend extracts and validates the token on every request.
3. If valid, the user's identity is set in Spring Security's context, and the request proceeds.
4. If the token is missing, expired, or invalid, the request is rejected with a 401 response.
5. The Axios response interceptor catches 401 errors, clears `localStorage`, and redirects to the login page.

### Route Protection

- The React app uses a `RouteGuard` component that checks if a user exists in the AuthContext.
- If no user is found (not logged in), the guard redirects to `/login`.
- Protected routes (like `/dashboard`) are wrapped with this guard.

## Key Business Rules

- Email must be unique across all users.
- Password must be at least 6 characters.
- Role is immutable after registration — a user cannot change from CANDIDATE to INTERVIEWER.
- JWT tokens expire after 24 hours — the user must log in again after expiration.
- The JWT contains the user's email (as the subject) and role (as a claim).
- Passwords are never stored in plain text — only BCrypt hashes are persisted.
