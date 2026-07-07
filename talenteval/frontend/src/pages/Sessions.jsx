import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import api from '../api/axios';

export default function Sessions() {
  const { user } = useAuth();
  const isInterviewer = user.role === 'INTERVIEWER';

  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // New session flow
  const [step, setStep] = useState(null); // null | 'select-candidate' | 'pick-questions' | 'interview' | 'view'
  const [candidates, setCandidates] = useState([]);
  const [activeSession, setActiveSession] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [selectedQuestions, setSelectedQuestions] = useState([]);
  const [currentQ, setCurrentQ] = useState(0);
  const [filterRole, setFilterRole] = useState('');

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
      const res = await api.post('/sessions', { candidateId });
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
      await api.put(`/sessions/${activeSession.id}/complete`);
      setStep(null);
      setActiveSession(null);
      fetchSessions();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to complete session');
    }
  };

  const viewSession = async (sessionId) => {
    try {
      const res = await api.get(`/sessions/${sessionId}`);
      setActiveSession(res.data);
      setCurrentQ(0);
      setStep('view');
    } catch {
      setError('Failed to load session');
    }
  };

  const goBack = () => {
    setStep(null);
    setActiveSession(null);
  };

  const statusClass = (s) => s === 'COMPLETED' ? 'badge badge-easy' : 'badge badge-medium';

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
          {candidates.length === 0 ? (
            <p className="empty-text">No candidates registered yet.</p>
          ) : (
            <div className="question-list">
              {candidates.map(c => (
                <div key={c.id} className="question-card" style={{ cursor: 'pointer' }}
                     onClick={() => selectCandidate(c.id)}>
                  <div className="question-content">
                    <p className="question-title">{c.name}</p>
                    <p style={{ fontSize: '14px', color: '#6b7280' }}>{c.email}</p>
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

  // Interview / View step
  if (step === 'interview' || step === 'view') {
    const qs = activeSession.questions;
    const current = qs[currentQ];
    const isLast = currentQ === qs.length - 1;
    const canComplete = isInterviewer && activeSession.status === 'IN_PROGRESS';

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
                    <span className="badge badge-topic">
                      {new Date(s.date).toLocaleDateString('en-IN', {
                        day: 'numeric', month: 'short', year: 'numeric',
                        hour: '2-digit', minute: '2-digit'
                      })}
                    </span>
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
