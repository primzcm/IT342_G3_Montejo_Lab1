import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { logoutUser } from "../../auth/api";
import { fetchCurrentUser, updateCurrentUser } from "../api";

function createForm(user = null) {
  return {
    firstname: user?.firstname || "",
    lastname: user?.lastname || "",
    bio: user?.bio || "",
    skills: user?.skills || ""
  };
}

function ProfilePage() {
  const navigate = useNavigate();
  const accessToken = localStorage.getItem("collabmatch_access_token") || localStorage.getItem("collabmatch_token");
  const refreshToken = localStorage.getItem("collabmatch_refresh_token");

  const [user, setUser] = useState(null);
  const [form, setForm] = useState(createForm());
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    if (!accessToken) {
      navigate("/login", { replace: true });
      return;
    }

    fetchCurrentUser(accessToken)
      .then((currentUser) => {
        setUser(currentUser);
        setForm(createForm(currentUser));
      })
      .catch((err) => {
        if (err.status === 401) {
          localStorage.removeItem("collabmatch_access_token");
          localStorage.removeItem("collabmatch_refresh_token");
          localStorage.removeItem("collabmatch_token");
          navigate("/login", { replace: true });
          return;
        }

        setError(err.message);
      })
      .finally(() => setLoading(false));
  }, [accessToken, navigate]);

  async function handleLogout() {
    try {
      if (accessToken && refreshToken) {
        await logoutUser({ accessToken, refreshToken });
      }
    } catch {
      // Ignore logout errors because client-side token removal is sufficient.
    } finally {
      localStorage.removeItem("collabmatch_access_token");
      localStorage.removeItem("collabmatch_refresh_token");
      localStorage.removeItem("collabmatch_token");
      navigate("/login", { replace: true });
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();

    try {
      setSaving(true);
      setError("");
      setSuccess("");
      const updatedUser = await updateCurrentUser(accessToken, form);
      setUser(updatedUser);
      setForm(createForm(updatedUser));
      setSuccess("Profile updated.");
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <main className="dashboard-page">
        <section className="card"><p>Loading profile...</p></section>
      </main>
    );
  }

  if (!user) {
    return (
      <main className="dashboard-page">
        <section className="card">
          <p className="error">{error || "Unable to load profile."}</p>
          <button type="button" onClick={() => navigate("/dashboard")}>Back to dashboard</button>
        </section>
      </main>
    );
  }

  return (
    <main className="project-detail-page min-h-screen bg-ink text-slate-100 antialiased">
      <header className="dashboard-topbar">
        <div className="dashboard-topbar-copy">
          <div className="dashboard-brand">CollabMatch</div>
          <div className="dashboard-topbar-summary">
            <span className="dashboard-topbar-pill">Profile</span>
            <p>
              {user.username}
              <span>•</span>
              {user.role}
            </p>
          </div>
        </div>
        <div className="dashboard-top-actions">
          <button type="button" className="topbar-action-button" onClick={() => navigate("/dashboard")}>
            Back to Dashboard
          </button>
          <button type="button" className="explorer-secondary-button" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <section className="project-detail-shell">
        <section className="profile-layout">
          <article className="project-panel">
            <div className="project-panel-heading">
              <div>
                <p className="project-panel-label">Account</p>
                <h2>{user.firstname} {user.lastname}</h2>
              </div>
            </div>

            <div className="project-metadata-grid">
              <div className="project-metadata-item">
                <span>Email</span>
                <strong>{user.email}</strong>
              </div>
              <div className="project-metadata-item">
                <span>Username</span>
                <strong>{user.username}</strong>
              </div>
            </div>
          </article>

          <article className="project-panel">
            <div className="project-panel-heading">
              <div>
                <p className="project-panel-label">Edit Profile</p>
                <h2>Public details and skills</h2>
              </div>
            </div>

            <form className="project-form" onSubmit={handleSubmit}>
              <label>
                First Name
                <input
                  type="text"
                  value={form.firstname}
                  onChange={(event) => setForm({ ...form, firstname: event.target.value })}
                  required
                />
              </label>

              <label>
                Last Name
                <input
                  type="text"
                  value={form.lastname}
                  onChange={(event) => setForm({ ...form, lastname: event.target.value })}
                  required
                />
              </label>

              <label>
                Bio
                <textarea
                  value={form.bio}
                  onChange={(event) => setForm({ ...form, bio: event.target.value })}
                  rows={4}
                />
              </label>

              <label>
                Skills
                <textarea
                  value={form.skills}
                  onChange={(event) => setForm({ ...form, skills: event.target.value })}
                  rows={4}
                  placeholder="React, UI/UX, Kotlin, Backend"
                />
              </label>

              {error ? <p className="dashboard-inline-error">{error}</p> : null}
              {success ? <p className="dashboard-inline-success">{success}</p> : null}

              <div className="project-form-actions">
                <button type="button" className="explorer-secondary-button" onClick={() => navigate("/dashboard")}>
                  Cancel
                </button>
                <button type="submit" className="explorer-primary-button" disabled={saving}>
                  {saving ? "Saving..." : "Save Profile"}
                </button>
              </div>
            </form>
          </article>
        </section>
      </section>
    </main>
  );
}

export default ProfilePage;
