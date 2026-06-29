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

Key settings to verify or update:

```properties
# MySQL connection — update password to match your MySQL root password
spring.datasource.url=jdbc:mysql://localhost:3306/talenteval?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

# JPA — auto-creates/updates tables
spring.jpa.hibernate.ddl-auto=update

# JWT — token valid for 24 hours (86400000 ms)
jwt.secret=TalentEvalSuperSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm
jwt.expiration=86400000
```

The only value you must change is `spring.datasource.password` — set it to your MySQL root password.

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

## Project Structure

```
talenteval/
├── docs/                    # Project documentation
├── frontend/                # React app (Vite)
│   ├── src/
│   │   ├── api/             # Axios HTTP client
│   │   ├── components/      # Shared components
│   │   ├── context/         # React context (auth state)
│   │   └── pages/           # Page components
│   └── package.json
├── src/main/java/com/talenteval/talenteval/
│   ├── controller/          # REST API controllers
│   ├── dto/                 # Request/response objects
│   ├── entity/              # JPA entities
│   ├── exception/           # Global error handling
│   ├── repository/          # Database access (JPA)
│   ├── security/            # JWT, filters, Spring Security config
│   └── service/             # Business logic
├── src/main/resources/
│   └── application.properties
├── pom.xml                  # Maven dependencies
└── PLAN.md                  # Project progress tracker
```

## Troubleshooting

| Problem | Solution |
|---|---|
| `Access denied for user 'root'` | Update `spring.datasource.password` in `application.properties` to match your MySQL password |
| `Out of memory` error on startup | Add `-Xmx384m` flag as shown above, or close other applications to free RAM |
| `Port 8080 already in use` | Stop the other process using port 8080, or add `server.port=8081` to `application.properties` |
| `Port 5173 already in use` | Stop the other Vite process, or run `npx vite --port 5174` |
| `CORS error` in browser console | Make sure the backend is running and `SecurityConfig.java` allows `http://localhost:5173` |
| Frontend shows blank page | Open browser dev tools (F12) → Console tab to check for JavaScript errors |
