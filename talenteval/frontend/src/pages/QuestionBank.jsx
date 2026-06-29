import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import api from '../api/axios';

export default function QuestionBank() {
  const { user } = useAuth();
  const isInterviewer = user.role === 'INTERVIEWER';

  const [questions, setQuestions] = useState([]);
  const [filterRole, setFilterRole] = useState('');
  const [filterTopic, setFilterTopic] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({ title: '', role: 'HR', topic: '', difficulty: 'EASY' });
  const [formError, setFormError] = useState('');

  const roles = ['HR', 'UX', 'PM', 'FINANCE', 'ENGINEERING'];
  const difficulties = ['EASY', 'MEDIUM', 'HARD'];

  const fetchQuestions = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {};
      if (filterRole) params.role = filterRole;
      if (filterTopic) params.topic = filterTopic;
      const res = await api.get('/questions', { params });
      setQuestions(res.data);
    } catch {
      setError('Failed to load questions');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQuestions();
  }, [filterRole, filterTopic]);

  const openAddForm = () => {
    setEditingId(null);
    setForm({ title: '', role: 'HR', topic: '', difficulty: 'EASY' });
    setFormError('');
    setShowForm(true);
  };

  const openEditForm = (q) => {
    setEditingId(q.id);
    setForm({ title: q.title, role: q.role, topic: q.topic, difficulty: q.difficulty });
    setFormError('');
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    try {
      if (editingId) {
        await api.put(`/questions/${editingId}`, form);
      } else {
        await api.post('/questions', form);
      }
      setShowForm(false);
      fetchQuestions();
    } catch (err) {
      const data = err.response?.data;
      if (data?.error) {
        setFormError(data.error);
      } else if (typeof data === 'object') {
        setFormError(Object.values(data).join('. '));
      } else {
        setFormError('Failed to save question');
      }
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this question?')) return;
    try {
      await api.delete(`/questions/${id}`);
      fetchQuestions();
    } catch {
      setError('Failed to delete question');
    }
  };

  const difficultyClass = (d) => {
    if (d === 'EASY') return 'badge badge-easy';
    if (d === 'MEDIUM') return 'badge badge-medium';
    return 'badge badge-hard';
  };

  return (
    <div className="dashboard">
      <Navbar />
      <div className="page-container">
      <div className="page-header">
        <h2>Question Bank</h2>
        {isInterviewer && (
          <button className="btn btn-primary" onClick={openAddForm}>Add Question</button>
        )}
      </div>

      <div className="filters">
        <select value={filterRole} onChange={(e) => setFilterRole(e.target.value)}>
          <option value="">All Roles</option>
          {roles.map((r) => (
            <option key={r} value={r}>{r}</option>
          ))}
        </select>
        <input
          type="text"
          placeholder="Filter by topic..."
          value={filterTopic}
          onChange={(e) => setFilterTopic(e.target.value)}
        />
      </div>

      {error && <div className="auth-error">{error}</div>}

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>{editingId ? 'Edit Question' : 'Add Question'}</h3>
            {formError && <div className="auth-error">{formError}</div>}
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="title">Question</label>
                <textarea
                  id="title"
                  rows={3}
                  value={form.title}
                  onChange={(e) => setForm({ ...form, title: e.target.value })}
                  required
                />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="qrole">Role</label>
                  <select
                    id="qrole"
                    value={form.role}
                    onChange={(e) => setForm({ ...form, role: e.target.value })}
                  >
                    {roles.map((r) => (
                      <option key={r} value={r}>{r}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label htmlFor="difficulty">Difficulty</label>
                  <select
                    id="difficulty"
                    value={form.difficulty}
                    onChange={(e) => setForm({ ...form, difficulty: e.target.value })}
                  >
                    {difficulties.map((d) => (
                      <option key={d} value={d}>{d}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="topic">Topic</label>
                <input
                  id="topic"
                  type="text"
                  value={form.topic}
                  onChange={(e) => setForm({ ...form, topic: e.target.value })}
                  placeholder="e.g. System Design, Behavioral"
                  required
                />
              </div>
              <div className="form-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">{editingId ? 'Save Changes' : 'Add Question'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {loading ? (
        <p className="loading-text">Loading questions...</p>
      ) : questions.length === 0 ? (
        <p className="empty-text">No questions found.</p>
      ) : (
        <div className="question-list">
          {questions.map((q) => (
            <div key={q.id} className="question-card">
              <div className="question-content">
                <p className="question-title">{q.title}</p>
                <div className="question-meta">
                  <span className="badge badge-role">{q.role}</span>
                  <span className="badge badge-topic">{q.topic}</span>
                  <span className={difficultyClass(q.difficulty)}>{q.difficulty}</span>
                </div>
              </div>
              {isInterviewer && (
                <div className="question-actions">
                  <button className="btn btn-sm btn-secondary" onClick={() => openEditForm(q)}>Edit</button>
                  <button className="btn btn-sm btn-danger" onClick={() => handleDelete(q.id)}>Delete</button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
    </div>
  );
}
