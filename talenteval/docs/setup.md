# Setup Guide — TalentEval

## Prerequisites

| Software | Version | Download |
|---|---|---|
| Java JDK | 17 or higher | https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html |
| MySQL | 8.0 or higher | https://dev.mysql.com/downloads/mysql/ |
| Node.js | 18 or higher | https://nodejs.org/ |
| npm | Comes with Node.js | — |

Verify installations:

```bash
java -version
mysql --version
node -v
npm -v
```

## Database Setup

1. Start MySQL and log in:

```bash
mysql -u root -p
```

2. No manual database creation is needed. The application will automatically create the `talenteval` database on first startup (configured via `createDatabaseIfNotExist=true` in the connection URL). Hibernate will create all tables automatically.

3. To verify after startup:

```sql
USE talenteval;
SHOW TABLES;
```

## Backend Configuration

The backend configuration file is located at:

```
talenteval/src/main/resources/application.properties
```

```properties
# MySQL connection — credentials come from environment variables, not hardcoded
spring.datasource.url=jdbc:mysql://localhost:3306/talenteval?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA — auto-creates/updates tables
spring.jpa.hibernate.ddl-auto=update

# JWT — token valid for 24 hours (86400000 ms)
jwt.secret=TalentEvalSuperSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm
jwt.expiration=86400000

# Mail — required for session-assignment and completion notifications
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Video recordings — max upload size
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Cloudinary — required for video recording storage
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}

# Frontend — used to build links in emails (password reset, session notifications)
app.frontend-url=${FRONTEND_URL:http://localhost:5173}
```

Required environment variables: `DB_USERNAME`, `DB_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`. They are intentionally kept out of `application.properties` so they never end up in source control.

For Gmail SMTP, `MAIL_USERNAME` is your Gmail address and `MAIL_PASSWORD` is a 16-character [App Password](https://myaccount.google.com/apppasswords) (not your normal Gmail password — this requires 2-Step Verification to be enabled on the account). For Cloudinary, all three values are shown on your [Cloudinary dashboard](https://cloudinary.com/console) after creating a free account.

### Recommended: `application-local.properties`

Rather than setting environment variables in every terminal session, create a git-ignored `talenteval/src/main/resources/application-local.properties` with all the real values:

```properties
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_16_char_app_password
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

This file is already covered by `.gitignore` (under `### Local Config ###`), so it's safe to keep real secrets in it. Run the backend with the `local` profile active so Spring picks it up:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Xmx384m" "-Dspring-boot.run.profiles=local"
```

### Alternative: setting environment variables manually (Windows / PowerShell)

Set them once, persistently, for your user account:

```powershell
[System.Environment]::SetEnvironmentVariable("DB_USERNAME", "root", "User")
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_mysql_password", "User")
```

**Restart your terminal (or VS Code) after running this** — persistent environment variables only take effect in new shell sessions.

If you don't want to restart, you can set them for just the current terminal session instead:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_mysql_password"
```

### Alternative: setting environment variables manually (macOS / Linux)

Add to your shell profile (`~/.bashrc`, `~/.zshrc`, etc.):

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
```

Then reload it: `source ~/.zshrc` (or open a new terminal).

## Running the Backend

Open a terminal and navigate to the project root:

```bash
cd talenteval
```

Run the Spring Boot application using the Maven wrapper:

**Windows:**
```bash
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Xmx384m"
```

**macOS / Linux:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx384m"
```

The `-Xmx384m` flag limits Java heap memory. Increase it if your system has more RAM available.

You should see output ending with:

```
Started TalentevalApplication in X seconds
```

The backend is now running at `http://localhost:8080`.

## Running the Frontend

Open a second terminal and navigate to the frontend folder:

```bash
cd talenteval/frontend
```

Install dependencies (first time only):

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

You should see:

```
VITE ready in Xms
➜ Local: http://localhost:5173/
```

The frontend is now running at `http://localhost:5173`.

## Testing the Application

### In the Browser

1. Open `http://localhost:5173` in your browser.
2. Click "Sign up" to create a new account.
3. Register as an INTERVIEWER or CANDIDATE.
4. You will be redirected to the Dashboard.

### Testing APIs Directly

You can test the REST API using curl, Postman, or any HTTP client.

**Register a new user:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "role": "INTERVIEWER"
  }'
```

**Login:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Access a protected endpoint (use the token from login response):**

```bash
curl http://localhost:8080/api/questions \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Request a password reset:**

```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

## Project Structure

```
talenteval/                  # git repo root
├── README.md                 # entry point — start here
├── PLAN.md                   # project progress tracker
└── talenteval/                # Spring Boot project root
    ├── docs/                  # project documentation (this folder)
    ├── frontend/              # React app (Vite)
    │   ├── src/
    │   │   ├── api/           # Axios HTTP client
    │   │   ├── components/    # Shared components
    │   │   ├── context/       # React context (auth state)
    │   │   └── pages/         # Page components
    │   └── package.json
    ├── src/main/java/com/talenteval/talenteval/
    │   ├── controller/        # REST API controllers
    │   ├── dto/                # Request/response objects
    │   ├── entity/              # JPA entities
    │   ├── exception/            # Global error handling
    │   ├── repository/            # Database access (JPA)
    │   ├── security/                # JWT, filters, Spring Security config
    │   └── service/                  # Business logic
    ├── src/main/resources/
    │   └── application.properties
    └── pom.xml                # Maven dependencies
```

## Troubleshooting

| Problem | Solution |
|---|---|
| `Access denied for user '${DB_USERNAME}'` | The `DB_USERNAME` / `DB_PASSWORD` environment variables aren't set in this terminal session — see [Setting the environment variables](#setting-the-environment-variables-windows--powershell) above |
| `Out of memory` error on startup | Add `-Xmx384m` flag as shown above, or close other applications to free RAM |
| `Port 8080 already in use` | Run `taskkill /F /IM java.exe` (Windows) to stop any leftover backend process, or add `server.port=8081` to `application.properties` |
| `Port 5173 already in use` | Vite will auto-pick the next free port (e.g. 5174) — if so, also add that origin to `SecurityConfig.java`'s CORS allowed origins list |
| `CORS error` in browser console | Make sure the backend is running and `SecurityConfig.java` allows the frontend's actual origin/port |
| Frontend shows blank page | Open browser dev tools (F12) → Console tab to check for JavaScript errors |
| Emails never arrive (no error shown to the user) | Email sending is `@Async` — a bad `MAIL_USERNAME`/`MAIL_PASSWORD` fails silently from the API's point of view. Check the backend console log for an async SMTP exception |
| Recording upload fails / playback is empty | Check `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` are set correctly — verify uploads appear under "Assets" in the Cloudinary dashboard |
