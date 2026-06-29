# Database Schema — TalentEval

## Database: `talenteval` (MySQL 8)

Tables are auto-created by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

---

## Table: `users`

Stores all registered users (both interviewers and candidates).

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| name | VARCHAR(255) | NOT NULL | Full name of the user |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Login email address |
| password | VARCHAR(255) | NOT NULL | BCrypt-hashed password |
| role | ENUM('CANDIDATE', 'INTERVIEWER') | NOT NULL | User's role in the system |

---

## Table: `questions`

Stores the question bank. Pre-seeded with default questions on first run.

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique question identifier |
| title | VARCHAR(500) | NOT NULL | The question text |
| role | ENUM('HR', 'UX', 'PM', 'FINANCE', 'ENGINEERING') | NOT NULL | Target interview role |
| topic | VARCHAR(255) | NOT NULL | Topic category (e.g., "Behavioral", "System Design") |
| difficulty | ENUM('EASY', 'MEDIUM', 'HARD') | NOT NULL | Difficulty level |

---

## Table: `sessions`

Stores mock interview sessions between an interviewer and a candidate.

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique session identifier |
| interviewer_id | BIGINT | NOT NULL, FOREIGN KEY → users(id) | The interviewer conducting the session |
| candidate_id | BIGINT | NOT NULL, FOREIGN KEY → users(id) | The candidate being interviewed |
| date | DATETIME | NOT NULL | When the session took place |
| status | ENUM('IN_PROGRESS', 'COMPLETED') | NOT NULL | Current state of the session |

---

## Table: `session_questions`

Join table linking sessions to questions, preserving question order.

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique row identifier |
| session_id | BIGINT | NOT NULL, FOREIGN KEY → sessions(id) | The session this question belongs to |
| question_id | BIGINT | NOT NULL, FOREIGN KEY → questions(id) | The question being used |
| question_order | INT | NOT NULL | Display order of the question within the session |

---

## Table: `scorecards`

Stores the interviewer's evaluation after a session.

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique scorecard identifier |
| session_id | BIGINT | NOT NULL, UNIQUE, FOREIGN KEY → sessions(id) | The session being evaluated (one scorecard per session) |
| candidate_id | BIGINT | NOT NULL, FOREIGN KEY → users(id) | The candidate being evaluated |
| communication | INT | NOT NULL, CHECK (1-5) | Rating for communication skills |
| structure | INT | NOT NULL, CHECK (1-5) | Rating for structured thinking |
| content | INT | NOT NULL, CHECK (1-5) | Rating for content quality |
| confidence | INT | NOT NULL, CHECK (1-5) | Rating for confidence level |
| comments | TEXT | NULLABLE | Optional written feedback |

---

## Foreign Key Relationships

```
sessions.interviewer_id  → users.id
sessions.candidate_id    → users.id
session_questions.session_id   → sessions.id
session_questions.question_id  → questions.id
scorecards.session_id    → sessions.id
scorecards.candidate_id  → users.id
```
