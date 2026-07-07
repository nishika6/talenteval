import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const location = useLocation();

  const isActive = (path) => location.pathname === path ? 'nav-link active' : 'nav-link';

  return (
    <header className="dashboard-header">
      <div className="header-left">
        <h1>TalentEval</h1>
        <nav className="nav-links">
          <Link to="/dashboard" className={isActive('/dashboard')}>Dashboard</Link>
          <Link to="/questions" className={isActive('/questions')}>Questions</Link>
          <Link to="/sessions" className={isActive('/sessions')}>Sessions</Link>
        </nav>
      </div>
      <div className="header-right">
        <span className="user-info">
          {user.name} <span className="role-badge">{user.role}</span>
        </span>
        <button onClick={logout} className="logout-btn">Sign Out</button>
      </div>
    </header>
  );
}
