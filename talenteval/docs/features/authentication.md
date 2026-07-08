# Feature: User Authentication

## What It Does

Allows users to create an account and log in to TalentEval. Every user selects a role at registration — either **CANDIDATE** or **INTERVIEWER** — which determines what they can do in the system. Authentication is handled via JWT tokens, and passwords are securely hashed with BCrypt. Users who forget their password can reset it via an emailed link.

## How It Works End to End

### Registration Flow

1. User opens the app and navigates to the **Register** page.
2. User fills in their full name, email, password, and selects a role (Candidate or Interviewer).
3. React sends a `POST /api/auth/register` request to the backend.
4. The backend validates the input (email format, password length, valid role).
5. The email is normalized (lowercased, and anything from `+` onward in the local part stripped) before the uniqueness check and before saving — so `nishika+test@gmail.com` is treated as the same account as `nishika@gmail.com`, since both deliver to the same inbox.
6. It checks if the (normalized) email is already registered — if so, returns an error.
7. The password is hashed using BCrypt and the user is saved to the `users` table in MySQL.
8. A JWT token is generated containing the user's email and role, signed with a secret key.
9. The token, name, email, and role are returned to the frontend.
10. React stores the token in `localStorage` and redirects to the Dashboard.

### Login Flow

1. User navigates to the **Login** page.
2. User enters their email and password.
3. React sends a `POST /api/auth/login` request. The email is normalized the same way as registration before lookup, so a `+alias` variant still resolves to the right account.
4. Spring Security's `AuthenticationManager` loads the user from the database (via `UserDetailsServiceImpl`) and compares the provided password against the stored BCrypt hash.
5. If credentials are valid, a new JWT is generated and returned.
6. React stores the token and redirects to the Dashboard.
7. If credentials are invalid, the backend returns a 401 error and the frontend displays "Invalid email or password."

### Forgot Password Flow

1. From the **Login** page, the user clicks "Forgot Password?" and enters their email on the **Forgot Password** page.
2. React sends a `POST /api/auth/forgot-password` request. The response is always the same generic message, whether or not the email is registered — this prevents an attacker from using the endpoint to discover which emails have accounts.
3. If the email does match a user, the backend generates a single-use token, saves it with a 30-minute expiry, and emails a reset link (`{frontend-url}/reset-password?token=...`) to that user.
4. The user clicks the link and lands on the **Reset Password** page, which reads the token from the URL.
5. The user enters and confirms a new password. React sends a `POST /api/auth/reset-password` request with the token and new password.
6. The backend validates the token isn't expired or already used, updates the password (BCrypt-hashed), and marks the token used so it can't be reused.
7. The user is redirected to **Login** to sign in with their new password.

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

- Email must be unique across all users, after normalization (lowercased, `+alias` suffix stripped from the local part).
- Password must be at least 6 characters — the same minimum applies to a password set via Forgot Password.
- Role is immutable after registration — a user cannot change from CANDIDATE to INTERVIEWER.
- JWT tokens expire after 24 hours — the user must log in again after expiration.
- The JWT contains the user's email (as the subject) and role (as a claim).
- Passwords are never stored in plain text — only BCrypt hashes are persisted.
- A password reset token is single-use and expires 30 minutes after being issued.
- The forgot-password endpoint never reveals whether a given email is registered.
