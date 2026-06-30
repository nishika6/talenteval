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

export default function Dashboard() {
  const { user } = useAuth();
  const isInterviewer = user.role === 'INTERVIEWER';

  const [candidates, setCandidates] = useState([]);
  const [selectedCandidateId, setSelectedCandidateId] = useState('');
  const [progress, setProgress] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isInterviewer) {
      api.get('/users/candidates')
        .then(res => setCandidates(res.data))
        .catch(() => setError('Failed to load candidates'));
      setLoading(false);
    } else {
      fetchProgress('/progress/me');
    }
  }, []);

  const fetchProgress = async (url) => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get(url);
      setProgress(res.data);
    } catch {
      setError('Failed to load progress');
      setProgress(null);
    } finally {
      setLoading(false);
    }
  };

  const handleCandidateSelect = (id) => {
    setSelectedCandidateId(id);
    if (id) fetchProgress(`/progress/candidate/${id}`);
    else setProgress(null);
  };

  const formatDate = (d) => new Date(d).toLocaleDateString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric'
  });

  return (
    <div className="dashboard">
      <Navbar />
      <main className="dashboard-main">
        <h2>Welcome, {user.name}!</h2>

        {isInterviewer && (
          <div className="filters" style={{ marginTop: '20px', marginBottom: '24px' }}>
            <select value={selectedCandidateId} onChange={(e) => handleCandidateSelect(e.target.value)}>
              <option value="">Select a candidate to view progress</option>
              {candidates.map(c => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
        )}

        {error && <div className="auth-error">{error}</div>}

        {isInterviewer && !selectedCandidateId ? (
          <p className="empty-text">Select a candidate above to view their progress.</p>
        ) : loading ? (
          <p className="loading-text">Loading progress...</p>
        ) : !progress || progress.totalSessions === 0 ? (
          <p className="empty-text">
            {isInterviewer
              ? 'This candidate has no completed sessions with scorecards yet.'
              : 'You have no completed sessions with scorecards yet. Once an interviewer scores a session, your progress will appear here.'}
          </p>
        ) : (
          <>
            <div className="progress-summary">
              <div className="summary-card">
                <span className="summary-value">{progress.totalSessions}</span>
                <span className="summary-label">Total Sessions</span>
              </div>
              {CRITERIA.map(c => (
                <div className="summary-card" key={c.key}>
                  <span className="summary-value">{progress.averageScores[c.key]}</span>
                  <span className="summary-label">{c.label} Avg</span>
                </div>
              ))}
            </div>

            <h3 style={{ margin: '32px 0 16px' }}>Score Trend Over Time</h3>
            <div className="trend-chart">
              {progress.sessions.map((s, i) => (
                <div className="trend-column" key={s.sessionId}>
                  <div className="trend-bars">
                    {CRITERIA.map(c => (
                      <div
                        key={c.key}
                        className={`trend-bar trend-bar-${c.key}`}
                        style={{ height: `${(s[c.key] / 5) * 100}%` }}
                        title={`${c.label}: ${s[c.key]}`}
                      />
                    ))}
                  </div>
                  <span className="trend-label">#{i + 1}</span>
                </div>
              ))}
            </div>
            <div className="trend-legend">
              {CRITERIA.map(c => (
                <span key={c.key} className="legend-item">
                  <span className={`legend-dot trend-bar-${c.key}`} /> {c.label}
                </span>
              ))}
            </div>

            <h3 style={{ margin: '32px 0 16px' }}>Session History</h3>
            <div className="question-list">
              {progress.sessions.slice().reverse().map(s => (
                <div key={s.sessionId} className="question-card">
                  <div className="question-content">
                    <p className="question-title">
                      {formatDate(s.date)} — Interviewer: {s.interviewerName}
                    </p>
                    <div className="scorecard-ratings" style={{ marginTop: '10px', marginBottom: 0 }}>
                      {CRITERIA.map(c => (
                        <div className="scorecard-rating-item" key={c.key}>
                          <span className="scorecard-rating-label">{c.label}</span>
                          <span className="scorecard-rating-value">{s[c.key]} / 5</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </main>
    </div>
  );
}
