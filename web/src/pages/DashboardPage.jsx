import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchCurrentUser, logoutUser } from "../services/api";

function DashboardPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("collabmatch_token");

    if (!token) {
      navigate("/login", { replace: true });
      return;
    }

    fetchCurrentUser(token)
      .then(setUser)
      .catch((err) => {
        setError(err.message);
        localStorage.removeItem("collabmatch_token");
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  async function handleLogout() {
    const token = localStorage.getItem("collabmatch_token");
    try {
      if (token) {
        await logoutUser(token);
      }
    } catch {
      // Ignore logout errors because client-side token removal is sufficient.
    } finally {
      localStorage.removeItem("collabmatch_token");
      navigate("/login", { replace: true });
    }
  }

  if (loading) {
    return <main className="dashboard-page"><section className="card"><p>Loading account...</p></section></main>;
  }

  if (error || !user) {
    return (
      <main className="dashboard-page">
        <section className="card">
          <p className="error">{error || "Unable to load account."}</p>
          <button onClick={() => navigate("/login", { replace: true })}>Back to login</button>
        </section>
      </main>
    );
  }

  return (
    <main className="dashboard-page">
      <section className="card">
        <h1>Welcome, {user.username}</h1>
        <p>Use CollabMatch to find collaborators and build project teams.</p>
        <p><strong>Email:</strong> {user.email}</p>
        <p><strong>User ID:</strong> {user.id}</p>
        <p><strong>Joined:</strong> {new Date(user.createdAt).toLocaleString()}</p>
        <button onClick={handleLogout}>Logout</button>
      </section>
    </main>
  );
}

export default DashboardPage;
