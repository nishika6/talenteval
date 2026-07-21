# ER Diagram — TalentEval

## Entity Relationship Diagram

```mermaid
erDiagram
    USERS {
        bigint id PK
        varchar name
        varchar email UK
        varchar password
        enum role "CANDIDATE | INTERVIEWER"
    }

    QUESTIONS {
        bigint id PK
        varchar title
        enum role "HR | UX | PM | FINANCE | ENGINEERING"
        varchar topic
        enum difficulty "EASY | MEDIUM | HARD"
        int time_limit "default 120"
    }

    SESSIONS {
        bigint id PK
        bigint interviewer_id FK
        bigint candidate_id FK
        datetime date
        datetime scheduled_at "nullable"
        enum status "IN_PROGRESS | COMPLETED"
    }

    SESSION_QUESTIONS {
        bigint id PK
        bigint session_id FK
        bigint question_id FK
        int question_order
    }

    SCORECARDS {
        bigint id PK
        bigint session_id FK "UNIQUE"
        bigint candidate_id FK
        int communication "1-5"
        int structure "1-5"
        int content "1-5"
        int confidence "1-5"
        text comments
    }

    SESSION_RECORDINGS {
        bigint id PK
        bigint session_id FK "UNIQUE with question_id"
        bigint question_id FK "UNIQUE with session_id"
        varchar file_path "Cloudinary secure_url"
        datetime uploaded_at
    }

    PASSWORD_RESET_TOKENS {
        bigint id PK
        varchar token UK
        bigint user_id FK
        datetime expiry_date
        boolean used
    }

    USERS ||--o{ SESSIONS : "interviews as interviewer"
    USERS ||--o{ SESSIONS : "participates as candidate"
    SESSIONS ||--o{ SESSION_QUESTIONS : "contains"
    QUESTIONS ||--o{ SESSION_QUESTIONS : "used in"
    SESSIONS ||--o| SCORECARDS : "has"
    USERS ||--o{ SCORECARDS : "evaluated as candidate"
    SESSIONS ||--o{ SESSION_RECORDINGS : "has"
    QUESTIONS ||--o{ SESSION_RECORDINGS : "answered by"
    USERS ||--o{ PASSWORD_RESET_TOKENS : "requests"
```

## Relationships Summary

| Relationship | Type | Description |
|---|---|---|
| User → Session (as interviewer) | One-to-Many | An interviewer can conduct many sessions |
| User → Session (as candidate) | One-to-Many | A candidate can participate in many sessions |
| Session → Session_Questions | One-to-Many | A session contains multiple questions |
| Question → Session_Questions | One-to-Many | A question can be used across multiple sessions |
| Session → Scorecard | One-to-One | Each session has exactly one scorecard |
| User → Scorecard (as candidate) | One-to-Many | A candidate can have many scorecards |
| Session → Session_Recordings | One-to-Many | A session can have one recording per question |
| Question → Session_Recordings | One-to-Many | A question can have recordings across multiple sessions |
| User → Password_Reset_Tokens | One-to-Many | A user can have multiple reset tokens over time (old ones just accumulate, unused/expired) |

## Key Constraints

- A user's email must be unique.
- Each session has exactly one interviewer and one candidate (both are users).
- A scorecard is unique per session (one scorecard per session).
- Question order within a session is tracked via the `question_order` column in the join table.
- Ratings on the scorecard are integers from 1 to 5.
- A recording is unique per `(session_id, question_id)` pair — one recording per question per session, overwritten on re-record.
- A password reset token is unique and single-use (`used` flag), with a 30-minute expiry.
