import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { loginUser } from "../services/api";

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = location.state?.from?.pathname || "/dashboard";
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await loginUser(form);
      localStorage.setItem("collabmatch_token", response.token);
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="card">
        <h1>Login to CollabMatch</h1>
        <p>Find teammates for creative, academic, and event-based projects.</p>
        <form onSubmit={handleSubmit}>
          <label>
            Username
            <input
              type="text"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
            />
          </label>
          {error && <p className="error">{error}</p>}
          <button disabled={loading} type="submit">
            {loading ? "Signing in..." : "Login"}
          </button>
        </form>
        <p>
          Need an account? <Link to="/register">Register</Link>
        </p>
      </section>
    </main>
  );
}

export default LoginPage;
