import { useAuth } from '../context/AuthContext';

export default function Dashboard() {
  const { user, logout } = useAuth();

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>TalentEval</h1>
        <div className="header-right">
          <span className="user-info">
            {user.name} <span className="role-badge">{user.role}</span>
          </span>
          <button onClick={logout} className="logout-btn">Sign Out</button>
        </div>
      </header>
      <main className="dashboard-main">
        <h2>Welcome, {user.name}!</h2>
        <p>Your dashboard will be built out as features are added.</p>
      </main>
    </div>
  );
}
