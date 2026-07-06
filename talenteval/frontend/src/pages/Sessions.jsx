import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import api from '../api/axios';

const CRITERIA = [
  { key: 'communication', label: 'Communication' },
  { key: 'structure', label: 'Structure' },
  { key: 'content', label: 'Content' },
  { key: 'confidence', label: 'Confidence' },
];

export default function Sessions() {
  const { user } = useAuth();
  const isInterviewer = user.role === 'INTERVIEWER';

  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // New session flow
  const [step, setStep] = useState(null); // null | 'select-candidate' | 'pick-questions' | 'interview' | 'view' | 'scorecard-form'
  const [candidates, setCandidates] = useState([]);
  const [activeSession, setActiveSession] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [selectedQuestions, setSelectedQuestions] = useState([]);
  const [currentQ, setCurrentQ] = useState(0);
  const [filterRole, setFilterRole] = useState('');
  const [scheduledAt, setScheduledAt] = useState('');

  // Scorecard
  const [scorecard, setScorecard] = useState(null);
  const [scorecardChecked, setScorecardChecked] = useState(false);
  const [scorecardForm, setScorecardForm] = useState({
    communication: 0, structure: 0, content: 0, confidence: 0, comments: ''
  });
  const [scorecardError, setScorecardError] = useState('');

  const roles = ['HR', 'UX', 'PM', 'FINANCE', 'ENGINEERING'];

  const fetchSessions = async () => {
    setLoading(true);
    try {
      const res = await api.get('/sessions');
      setSessions(res.data);
    } catch {
      setError('Failed to load sessions');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchSessions(); }, []);

  const startNewSession = async () => {
    try {
      const res = await api.get('/users/candidates');
      setCandidates(res.data);
      setStep('select-candidate');
    } catch {
      setError('Failed to load candidates');
    }
  };

  const selectCandidate = async (candidateId) => {
    try {
      const body = { candidateId };
      if (scheduledAt) body.scheduledAt = scheduledAt;
      const res = await api.post('/sessions', body);
      setActiveSession(res.data);
      const qRes = await api.get('/questions', { params: filterRole ? { role: filterRole } : {} });
      setQuestions(qRes.data);
      setSelectedQuestions([]);
      setStep('pick-questions');
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to start session');
    }
  };

  const fetchFilteredQuestions = async (role) => {
    setFilterRole(role);
    try {
      const res = await api.get('/questions', { params: role ? { role } : {} });
      setQuestions(res.data);
    } catch {
      setError('Failed to load questions');
    }
  };

  const toggleQuestion = (id) => {
    setSelectedQuestions(prev =>
      prev.includes(id) ? prev.filter(q => q !== id) : [...prev, id]
    );
  };

  const submitQuestions = async () => {
    if (selectedQuestions.length === 0) return;
    try {
      const res = await api.post(`/sessions/${activeSession.id}/questions`, {
        questionIds: selectedQuestions
      });
      setActiveSession(res.data);
      setCurrentQ(0);
      setStep('interview');
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to add questions');
    }
  };

  const completeSession = async () => {
    try {
      const res = await api.put(`/sessions/${activeSession.id}/complete`);
      setActiveSession(res.data);
      if (isInterviewer) {
        setScorecard(null);
        setScorecardChecked(true);
        setScorecardForm({ communication: 0, structure: 0, content: 0, confidence: 0, comments: '' });
        setScorecardError('');
        setStep('scorecard-form');
      } else {
        goBack();
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to complete session');
    }
  };

  const loadScorecard = async (sessionId) => {
    setScorecardChecked(false);
    try {
      const res = await api.get(`/scorecards/session/${sessionId}`);
      setScorecard(res.data);
    } catch {
      setScorecard(null);
    } finally {
      setScorecardChecked(true);
    }
  };

  const viewSession = async (sessionId) => {
    try {
      const res = await api.get(`/sessions/${sessionId}`);
      setActiveSession(res.data);
      setCurrentQ(0);
      setStep('view');
      if (res.data.status === 'COMPLETED') {
        loadScorecard(sessionId);
      } else {
        setScorecard(null);
        setScorecardChecked(true);
      }
    } catch {
      setError('Failed to load session');
    }
  };

  const goBack = () => {
    setStep(null);
    setActiveSession(null);
    setScorecard(null);
    setScheduledAt('');
    fetchSessions();
  };

  const setRating = (key, value) => {
    setScorecardForm(prev => ({ ...prev, [key]: value }));
  };

  const submitScorecard = async () => {
    setScorecardError('');
    const { communication, structure, content, confidence } = scorecardForm;
    if (!communication || !structure || !content || !confidence) {
      setScorecardError('Please rate all four criteria before submitting.');
      return;
    }
    try {
      const res = await api.post('/scorecards', {
        sessionId: activeSession.id,
        ...scorecardForm
      });
      setScorecard(res.data);
      setStep('view');
    } catch (err) {
      const data = err.response?.data;
      if (data?.error) {
        setScorecardError(data.error);
      } else if (typeof data === 'object') {
        setScorecardError(Object.values(data).join('. '));
      } else {
        setScorecardError('Failed to submit scorecard');
      }
    }
  };

  const statusClass = (s) => s === 'COMPLETED' ? 'badge badge-easy' : 'badge badge-medium';

  const formatDateTime = (d) => new Date(d).toLocaleString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });

  // Select candidate step
  if (step === 'select-candidate') {
    return (
      <div className="dashboard">
        <Navbar />
        <div className="page-container">
          <div className="page-header">
            <h2>Select a Candidate</h2>
            <button className="btn btn-secondary" onClick={goBack}>Cancel</button>
          </div>
          <div className="filters" style={{ marginBottom: '24px' }}>
            <label style={{ fontSize: '14px', color: 'var(--text-light)', marginBottom: '6px', display: 'block' }}>
              Schedule date &amp; time
            </label>
            <input
              type="datetime-local"
              value={scheduledAt}
              onChange={(e) => setScheduledAt(e.target.value)}
            />
          </div>
          {candidates.length === 0 ? (
            <p className="empty-text">No candidates registered yet.</p>
          ) : (
            <div className="question-list">
              {candidates.map(c => (
                <div key={c.id} className="question-card" style={{ cursor: 'pointer' }}
                     onClick={() => selectCandidate(c.id)}>
                  <div className="question-content">
                    <p className="question-title">{c.name}</p>
                    <p style={{ fontSize: '14px', color: 'var(--text-light)' }}>{c.email}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    );
  }

  // Pick questions step
  if (step === 'pick-questions') {
    return (
      <div className="dashboard">
        <Navbar />
        <div className="page-container">
          <div className="page-header">
            <h2>Pick Questions for {activeSession.candidateName}</h2>
            <div style={{ display: 'flex', gap: '8px' }}>
              <button className="btn btn-secondary" onClick={goBack}>Cancel</button>
              <button className="btn btn-primary" onClick={submitQuestions}
                      disabled={selectedQuestions.length === 0}>
                Start Interview ({selectedQuestions.length})
              </button>
            </div>
          </div>
          <div className="filters">
            <select value={filterRole} onChange={(e) => fetchFilteredQuestions(e.target.value)}>
              <option value="">All Roles</option>
              {roles.map(r => <option key={r} value={r}>{r}</option>)}
            </select>
          </div>
          <div className="question-list">
            {questions.map(q => (
              <div key={q.id} className={`question-card ${selectedQuestions.includes(q.id) ? 'selected' : ''}`}
                   style={{ cursor: 'pointer' }} onClick={() => toggleQuestion(q.id)}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flex: 1 }}>
                  <input type="checkbox" checked={selectedQuestions.includes(q.id)}
                         onChange={() => toggleQuestion(q.id)} onClick={e => e.stopPropagation()} />
                  <div className="question-content">
                    <p className="question-title">{q.title}</p>
                    <div className="question-meta">
                      <span className="badge badge-role">{q.role}</span>
                      <span className="badge badge-topic">{q.topic}</span>
                      <span className={`badge badge-${q.difficulty.toLowerCase()}`}>{q.difficulty}</span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  // Scorecard form step
  if (step === 'scorecard-form') {
    return (
      <div className="dashboard">
        <Navbar />
        <div className="page-container">
          <div className="page-header">
            <h2>Scorecard for {activeSession.candidateName}</h2>
            <button className="btn btn-secondary" onClick={goBack}>Skip for Now</button>
          </div>

          <div className="interview-card">
            {scorecardError && <div className="auth-error">{scorecardError}</div>}

            {CRITERIA.map(c => (
              <div className="rating-row" key={c.key}>
                <label>{c.label}</label>
                <div className="rating-buttons">
                  {[1, 2, 3, 4, 5].map(n => (
                    <button
                      type="button"
                      key={n}
                      className={`rating-btn ${scorecardForm[c.key] === n ? 'active' : ''}`}
                      onClick={() => setRating(c.key, n)}
                    >
                      {n}
                    </button>
                  ))}
                </div>
              </div>
            ))}

            <div className="form-group" style={{ marginTop: '20px' }}>
              <label htmlFor="comments">Comments (optional)</label>
              <textarea
                id="comments"
                rows={4}
                value={scorecardForm.comments}
                onChange={(e) => setScorecardForm({ ...scorecardForm, comments: e.target.value })}
                placeholder="Additional feedback for the candidate..."
              />
            </div>

            <div className="form-actions">
              <button className="btn btn-primary" onClick={submitScorecard}>Submit Scorecard</button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Interview / View step
  if (step === 'interview' || step === 'view') {
    const qs = activeSession.questions;
    const current = qs[currentQ];
    const isLast = currentQ === qs.length - 1;
    const canComplete = activeSession.status === 'IN_PROGRESS';
    const canFillScorecard = isInterviewer && activeSession.status === 'COMPLETED'
      && scorecardChecked && !scorecard;

    return (
      <div className="dashboard">
        <Navbar />
        <div className="page-container">
          <div className="page-header">
            <h2>
              {step === 'view' ? 'Session Review' : 'Interview'}: {activeSession.candidateName}
            </h2>
            <button className="btn btn-secondary" onClick={goBack}>
              {step === 'view' ? 'Back' : 'Exit'}
            </button>
          </div>

          <div className="session-info">
            <span>Interviewer: <strong>{activeSession.interviewerName}</strong></span>
            <span>Candidate: <strong>{activeSession.candidateName}</strong></span>
            {activeSession.scheduledAt && (
              <span>Scheduled: <strong>{formatDateTime(activeSession.scheduledAt)}</strong></span>
            )}
            <span className={statusClass(activeSession.status)}>{activeSession.status.replace('_', ' ')}</span>
          </div>

          {qs.length > 0 ? (
            <div className="interview-card">
              <div className="interview-progress">
                Question {currentQ + 1} of {qs.length}
              </div>
              <div className="interview-question">
                <p className="question-title" style={{ fontSize: '18px' }}>{current.title}</p>
                <div className="question-meta" style={{ marginTop: '12px' }}>
                  <span className="badge badge-role">{current.role}</span>
                  <span className="badge badge-topic">{current.topic}</span>
                  <span className={`badge badge-${current.difficulty.toLowerCase()}`}>{current.difficulty}</span>
                </div>
              </div>
              <div className="interview-nav">
                <button className="btn btn-secondary" onClick={() => setCurrentQ(currentQ - 1)}
                        disabled={currentQ === 0}>Previous</button>
                {isLast && canComplete ? (
                  <button className="btn btn-primary" onClick={completeSession}>Complete Session</button>
                ) : (
                  <button className="btn btn-primary" onClick={() => setCurrentQ(currentQ + 1)}
                          disabled={isLast}>Next</button>
                )}
              </div>
            </div>
          ) : (
            <p className="empty-text">No questions in this session.</p>
          )}

          {activeSession.status === 'COMPLETED' && scorecardChecked && (
            <div className="scorecard-section">
              <h3 style={{ marginBottom: '16px' }}>Scorecard</h3>
              {scorecard ? (
                <div className="interview-card">
                  <div className="scorecard-ratings">
                    {CRITERIA.map(c => (
                      <div className="scorecard-rating-item" key={c.key}>
                        <span className="scorecard-rating-label">{c.label}</span>
                        <span className="scorecard-rating-value">{scorecard[c.key]} / 5</span>
                      </div>
                    ))}
                  </div>
                  {scorecard.comments && (
                    <div className="scorecard-comments">
                      <p style={{ fontWeight: 600, marginBottom: '6px' }}>Comments</p>
                      <p>{scorecard.comments}</p>
                    </div>
                  )}
                </div>
              ) : canFillScorecard ? (
                <div className="interview-card" style={{ textAlign: 'center' }}>
                  <p style={{ marginBottom: '16px', color: 'var(--text-light)' }}>
                    No scorecard submitted yet for this session.
                  </p>
                  <button className="btn btn-primary" onClick={() => {
                    setScorecardForm({ communication: 0, structure: 0, content: 0, confidence: 0, comments: '' });
                    setScorecardError('');
                    setStep('scorecard-form');
                  }}>Fill Scorecard</button>
                </div>
              ) : (
                <p className="empty-text">Scorecard not yet submitted.</p>
              )}
            </div>
          )}
        </div>
      </div>
    );
  }

  // Session list (default)
  return (
    <div className="dashboard">
      <Navbar />
      <div className="page-container">
        <div className="page-header">
          <h2>Sessions</h2>
          {isInterviewer && (
            <button className="btn btn-primary" onClick={startNewSession}>Start New Session</button>
          )}
        </div>

        {error && <div className="auth-error">{error}</div>}

        {loading ? (
          <p className="loading-text">Loading sessions...</p>
        ) : sessions.length === 0 ? (
          <p className="empty-text">No sessions yet.</p>
        ) : (
          <div className="question-list">
            {sessions.map(s => (
              <div key={s.id} className="question-card" style={{ cursor: 'pointer' }}
                   onClick={() => viewSession(s.id)}>
                <div className="question-content">
                  <p className="question-title">
                    {isInterviewer ? s.candidateName : s.interviewerName}
                  </p>
                  <div className="question-meta">
                    {s.scheduledAt ? (
                      <span className="badge badge-topic">Scheduled: {formatDateTime(s.scheduledAt)}</span>
                    ) : (
                      <span className="badge badge-topic">Created: {formatDateTime(s.date)}</span>
                    )}
                    <span className={statusClass(s.status)}>{s.status.replace('_', ' ')}</span>
                    <span className="badge badge-role">{s.questions.length} questions</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
