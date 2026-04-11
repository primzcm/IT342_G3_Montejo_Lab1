import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createProject, fetchCurrentUser, fetchProjects, logoutUser, requestJoinProject } from "../services/api";

const skillOptions = ["React", "Solidity", "Python", "Design", "AI", "Data"];

function createInitialForm() {
  return {
    title: "",
    category: "FinTech",
    rolesNeeded: "",
    description: ""
  };
}

function getProjectMark(category) {
  const normalized = category.toLowerCase();
  if (normalized.includes("fin")) {
    return "fin";
  }
  if (normalized.includes("health")) {
    return "health";
  }
  if (normalized.includes("web3") || normalized.includes("security")) {
    return "shield";
  }
  if (normalized.includes("space")) {
    return "nebula";
  }
  if (normalized.includes("energy")) {
    return "solar";
  }
  return "neural";
}

function getProjectIcons(category) {
  const normalized = category.toLowerCase();
  if (normalized.includes("fin")) {
    return ["⌘", "◫", "◎"];
  }
  if (normalized.includes("health")) {
    return ["✚", "◔", "◉"];
  }
  if (normalized.includes("space")) {
    return ["✺", "⌁", "◐"];
  }
  if (normalized.includes("web3") || normalized.includes("security")) {
    return ["⬣", "⌲", "⬡"];
  }
  if (normalized.includes("energy")) {
    return ["✦", "◈", "⬢"];
  }
  return ["⚙", "◌", "⚗"];
}

function DashboardPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [projects, setProjects] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [submittingProject, setSubmittingProject] = useState(false);
  const [joiningProjectId, setJoiningProjectId] = useState(null);
  const [showProjectForm, setShowProjectForm] = useState(false);
  const [projectForm, setProjectForm] = useState(createInitialForm());
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [selectedSkill, setSelectedSkill] = useState("React");

  const accessToken = localStorage.getItem("collabmatch_access_token") || localStorage.getItem("collabmatch_token");
  const refreshToken = localStorage.getItem("collabmatch_refresh_token");

  useEffect(() => {
    if (!accessToken) {
      navigate("/login", { replace: true });
      return;
    }

    fetchCurrentUser(accessToken)
      .then((currentUser) => {
        setUser(currentUser);
        return fetchProjects(accessToken)
          .then((projectList) => setProjects(projectList))
          .catch((err) => {
            setError(err.message);
          });
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

  async function handleCreateProject(event) {
    event.preventDefault();
    if (!accessToken) {
      return;
    }

    try {
      setSubmittingProject(true);
      setError("");
      const createdProject = await createProject(accessToken, projectForm);
      setProjects((currentProjects) => [createdProject, ...currentProjects]);
      setProjectForm(createInitialForm());
      setShowProjectForm(false);
      setSelectedCategory("All");
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmittingProject(false);
    }
  }

  async function handleJoinProject(projectId) {
    if (!accessToken) {
      return;
    }

    try {
      setJoiningProjectId(projectId);
      setError("");
      await requestJoinProject(accessToken, projectId, { message: "" });
      setProjects((currentProjects) =>
        currentProjects.map((project) =>
          project.id === projectId ? { ...project, joinRequested: true } : project
        )
      );
    } catch (err) {
      setError(err.message);
    } finally {
      setJoiningProjectId(null);
    }
  }

  const categories = ["All", ...new Set(projects.map((project) => project.category))];
  const filteredProjects = projects.filter((project) => {
    const categoryMatches = selectedCategory === "All" || project.category === selectedCategory;
    const skillMatches =
      !selectedSkill ||
      project.rolesNeeded.toLowerCase().includes(selectedSkill.toLowerCase()) ||
      project.description.toLowerCase().includes(selectedSkill.toLowerCase());
    return categoryMatches && skillMatches;
  });

  if (loading) {
    return <main className="dashboard-page"><section className="card"><p>Loading account...</p></section></main>;
  }

  if (!user) {
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
    <main className="dashboard-page dashboard-explorer">
      <header className="dashboard-topbar">
        <div className="dashboard-brand">CollabMatch</div>
        <nav className="dashboard-topnav">
          <a href="#projects" className="is-active">All Projects</a>
          <a href="#applications">My Applications</a>
          <a href="#saved">Saved</a>
          <a href="#messages">Messages</a>
        </nav>
        <div className="dashboard-top-actions">
          <button type="button" className="icon-button" aria-label="Notifications">🔔</button>
          <button type="button" className="icon-button dashboard-avatar" aria-label="Account menu">
            {user.firstname?.[0]}{user.lastname?.[0]}
          </button>
        </div>
      </header>

      <div className="dashboard-layout">
        <aside className="dashboard-sidebar">
          <div>
            <h1 className="sidebar-title">Project Explorer</h1>
            <p className="sidebar-subtitle">Find your next catalyst</p>
          </div>

          <div className="sidebar-nav">
            <button type="button" className="sidebar-link is-active">
              <span>▦</span>
              All Projects
            </button>
            <button type="button" className="sidebar-link">
              <span>☑</span>
              My Applications
            </button>
            <button type="button" className="sidebar-link">
              <span>🔖</span>
              Saved
            </button>
            <button type="button" className="sidebar-link">
              <span>🗨</span>
              Messages
            </button>
          </div>

          <div className="sidebar-divider" />

          <section className="filter-panel">
            <p className="filter-eyebrow">FILTERS</p>
            <label className="filter-label">
              Category
              <select
                className="filter-select"
                value={selectedCategory}
                onChange={(event) => setSelectedCategory(event.target.value)}
              >
                {categories.map((category) => (
                  <option key={category} value={category}>{category}</option>
                ))}
              </select>
            </label>

            <div className="filter-label">
              Skills Required
              <div className="skill-chips">
                {skillOptions.map((skill) => (
                  <button
                    key={skill}
                    type="button"
                    className={`skill-chip ${selectedSkill === skill ? "is-active" : ""}`}
                    onClick={() => setSelectedSkill(skill)}
                  >
                    {skill}
                  </button>
                ))}
              </div>
            </div>
          </section>

          <div className="sidebar-divider" />

          <button type="button" className="sidebar-post-button" onClick={() => setShowProjectForm(true)}>
            Post a Project
          </button>

          <div className="sidebar-footer-links">
            <button type="button" className="sidebar-footer-link">
              <span>⚙</span>
              Settings
            </button>
            <button type="button" className="sidebar-footer-link" onClick={handleLogout}>
              <span>↩</span>
              Logout
            </button>
          </div>
        </aside>

        <section className="dashboard-content">
          <section className="dashboard-hero" id="projects">
            <p className="dashboard-overline">Welcome back, {user.firstname}</p>
            <h2>
              Discover the
              <span className="dashboard-hero-accent"> Future.</span>
            </h2>
            <p>
              Curated high-impact collaborations across the global tech landscape.
              Find teams that catalyze change.
            </p>
          </section>

          {error ? <p className="dashboard-inline-error">{error}</p> : null}

          <section className="dashboard-card-grid">
            {filteredProjects.map((project, index) => (
              <article key={project.title} className="explorer-card">
                <div className={`explorer-logo logo-${getProjectMark(project.category)}`}>
                  <span>{project.category}</span>
                </div>
                {project.category ? <div className="explorer-badge">{project.category}</div> : null}
                <h3>{project.title}</h3>
                <p>{project.description}</p>
                <p className="explorer-meta">
                  By {project.ownerName}
                  <span>•</span>
                  Roles: {project.rolesNeeded}
                </p>

                <div className="explorer-icons">
                  {getProjectIcons(project.category).map((icon) => (
                    <span key={icon}>{icon}</span>
                  ))}
                </div>

                <div className="explorer-actions">
                  <button type="button" className="explorer-secondary-button">View Detail</button>
                  <button
                    type="button"
                    className="explorer-primary-button"
                    disabled={project.owner || project.joinRequested || joiningProjectId === project.id}
                    onClick={() => handleJoinProject(project.id)}
                  >
                    {project.owner
                      ? "Owner"
                      : project.joinRequested
                        ? "Requested"
                        : joiningProjectId === project.id
                          ? "Joining..."
                          : "Join"}
                  </button>
                </div>

                {index === 5 ? (
                  <div className="explorer-floating-note">14 Developers Active Now</div>
                ) : null}
              </article>
            ))}

            {filteredProjects.length === 0 ? (
              <article className="explorer-empty-state">
                <h3>No projects yet</h3>
                <p>Create the first project post and it will appear here once saved to the database.</p>
                <button type="button" className="explorer-primary-button" onClick={() => setShowProjectForm(true)}>
                  Post a Project
                </button>
              </article>
            ) : null}
          </section>
        </section>
      </div>

      {showProjectForm ? (
        <div className="project-modal-backdrop" onClick={() => setShowProjectForm(false)}>
          <section className="project-modal" onClick={(event) => event.stopPropagation()}>
            <div className="project-modal-header">
              <div>
                <h2>Create Project</h2>
                <p>Publish a new collaboration opportunity and store it directly in the database.</p>
              </div>
              <button type="button" className="project-modal-close" onClick={() => setShowProjectForm(false)}>✕</button>
            </div>

            <form className="project-form" onSubmit={handleCreateProject}>
              <label>
                Project Title
                <input
                  type="text"
                  value={projectForm.title}
                  onChange={(event) => setProjectForm({ ...projectForm, title: event.target.value })}
                  required
                />
              </label>

              <label>
                Category
                <input
                  type="text"
                  value={projectForm.category}
                  onChange={(event) => setProjectForm({ ...projectForm, category: event.target.value })}
                  required
                />
              </label>

              <label>
                Skills / Roles Needed
                <input
                  type="text"
                  value={projectForm.rolesNeeded}
                  onChange={(event) => setProjectForm({ ...projectForm, rolesNeeded: event.target.value })}
                  placeholder="React, UI/UX, Backend"
                  required
                />
              </label>

              <label>
                Description
                <textarea
                  value={projectForm.description}
                  onChange={(event) => setProjectForm({ ...projectForm, description: event.target.value })}
                  rows="5"
                  required
                />
              </label>

              <div className="project-form-actions">
                <button type="button" className="explorer-secondary-button" onClick={() => setShowProjectForm(false)}>
                  Cancel
                </button>
                <button type="submit" className="explorer-primary-button" disabled={submittingProject}>
                  {submittingProject ? "Posting..." : "Publish Project"}
                </button>
              </div>
            </form>
          </section>
        </div>
      ) : null}
    </main>
  );
}

export default DashboardPage;
