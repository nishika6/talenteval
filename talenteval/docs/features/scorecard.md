# Feature: Scorecard

## What It Does

After completing a mock interview session, the interviewer fills out a structured scorecard evaluating the candidate across four fixed criteria — Communication, Structure, Content, and Confidence — each rated on a scale of 1 to 5. The interviewer can also add optional written comments. The scorecard is permanently linked to the session and the candidate's profile for future reference and progress tracking.

## How It Works End to End

### Filling Out a Scorecard

1. Immediately after marking a session as completed, the interviewer is taken straight to the scorecard form (they can choose "Skip for Now" to fill it in later instead).
2. The scorecard form shows four rating selectors (Communication, Structure, Content, Confidence), each a 1-5 button group, and an optional comments text area.
3. The interviewer rates each criterion and optionally writes feedback. All four ratings must be selected before submitting.
4. React sends a `POST /api/scorecards` request with the session ID, four ratings, and comments.
5. The backend validates that:
   - The session exists and is completed.
   - The current user is the interviewer who conducted the session.
   - No scorecard already exists for this session.
   - All ratings are between 1 and 5.
6. The scorecard is saved to the `scorecards` table, linked to both the session and the candidate.
7. The interviewer is taken to the session view, which now shows the scorecard inline.

### Filling Out a Scorecard Later

1. If the interviewer skips the scorecard right after completing the session, the session shows a "Fill Scorecard" prompt the next time they open it (as long as no scorecard exists yet).
2. Clicking it opens the same scorecard form described above.

### Viewing a Scorecard (Interviewer)

1. On the session detail page, the interviewer can view the scorecard they submitted.
2. The scorecard shows all four ratings and the comments.
3. Interviewers can also view scorecards for any candidate via the candidate's profile.

### Viewing a Scorecard (Candidate)

1. Candidates see their scorecards on the session detail page in a read-only view.
2. They can see all four ratings and any comments left by the interviewer.
3. Candidates can only view scorecards for sessions they participated in.

## The Four Criteria

| Criterion | What It Measures |
|---|---|
| **Communication** | Clarity of expression, articulation, listening skills, ability to explain concepts |
| **Structure** | Organized thinking, logical flow of answers, systematic problem-solving approach |
| **Content** | Depth of knowledge, accuracy of information, relevance of answers |
| **Confidence** | Composure under pressure, assertiveness, body language, conviction in responses |

## Key Business Rules

- Only the interviewer who conducted the session can submit a scorecard for it.
- A scorecard can only be submitted for a session with `COMPLETED` status.
- Each session can have exactly one scorecard (enforced by a unique constraint on `session_id`).
- All four ratings are required and must be integers from 1 to 5.
- The comments field is optional.
- Scorecards cannot be edited or deleted once submitted.
- Candidates can view their own scorecards but cannot view other candidates' scorecards.
- Interviewers can view scorecards for any candidate.
