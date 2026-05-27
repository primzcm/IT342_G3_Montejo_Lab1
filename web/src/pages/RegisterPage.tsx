import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../services/api";

function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ firstname: "", lastname: "", email: "", password: "", confirmPassword: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      if (form.password !== form.confirmPassword) {
        throw new Error("Passwords do not match");
      }

      const response = await registerUser({
        firstname: form.firstname,
        lastname: form.lastname,
        email: form.email,
        password: form.password
      });
      localStorage.setItem("collabmatch_access_token", response.accessToken);
      localStorage.setItem("collabmatch_refresh_token", response.refreshToken);
      navigate("/dashboard");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page min-h-screen bg-ink px-6 py-10 text-slate-100 antialiased">
      <section className="card">
        <h1>Create your CollabMatch account</h1>
        <p>Build your profile and connect with teammates based on skills and interests.</p>
        <form onSubmit={handleSubmit}>
          <label>
            First name
            <input
              type="text"
              value={form.firstname}
              onChange={(e) => setForm({ ...form, firstname: e.target.value })}
              required
            />
          </label>
          <label>
            Last name
            <input
              type="text"
              value={form.lastname}
              onChange={(e) => setForm({ ...form, lastname: e.target.value })}
              required
            />
          </label>
          <label>
            Email
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
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
              minLength={8}
            />
          </label>
          <label>
            Confirm password
            <input
              type="password"
              value={form.confirmPassword}
              onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
              required
              minLength={8}
            />
          </label>
          {error && <p className="error">{error}</p>}
          <button disabled={loading} type="submit">
            {loading ? "Creating..." : "Register"}
          </button>
        </form>
        <p>
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </section>
    </main>
  );
}

export default RegisterPage;
