import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';

export default function Dashboard() {
  const { user } = useAuth();

  return (
    <div className="dashboard">
      <Navbar />
      <main className="dashboard-main">
        <h2>Welcome, {user.name}!</h2>
        <p>Your dashboard will be built out as features are added.</p>
      </main>
    </div>
  );
}
