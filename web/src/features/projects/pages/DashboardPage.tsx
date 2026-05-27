import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { logoutUser } from "../../auth/api";
import { fetchCurrentUser } from "../../users/api";
import { createProject, fetchProjects, requestJoinProject } from "../api";
import { matchesProjectFilters, parseSkills } from "../projectSkills";

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
  const sections = [
    { id: "all", label: "All Projects" },
    { id: "mine", label: "My Projects" },
    { id: "joined", label: "Joined Projects" },
    { id: "applications", label: "My Applications" }
  ];
  const [user, setUser] = useState(null);
  const [projects, setProjects] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [submittingProject, setSubmittingProject] = useState(false);
  const [joiningProjectId, setJoiningProjectId] = useState(null);
  const [showProjectForm, setShowProjectForm] = useState(false);
  const [projectForm, setProjectForm] = useState(createInitialForm());
  const [activeSection, setActiveSection] = useState("all");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [skillQuery, setSkillQuery] = useState("");

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
      const createdProject = await createProject(accessToken, {
        ...projectForm,
        requiredSkills: parseSkills(projectForm.rolesNeeded)
      });
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

  function handleOpenProject(projectId) {
    navigate(`/projects/${projectId}`);
  }

  function handleOpenProfile() {
    navigate("/profile");
  }

  const categories = ["All", ...new Set(projects.map((project) => project.category))];
  const filteredProjects = projects.filter((project) =>
    matchesProjectFilters(project, selectedCategory, skillQuery)
  );

  const myProjects = filteredProjects.filter((project) => project.owner);
  const joinedProjects = filteredProjects.filter((project) => project.joined);
  const myApplications = filteredProjects.filter((project) => project.joinRequested);
  const visibleProjects =
    activeSection === "mine"
      ? myProjects
      : activeSection === "joined"
        ? joinedProjects
      : activeSection === "applications"
        ? myApplications
        : filteredProjects;
  const activeSectionConfig = sections.find((section) => section.id === activeSection) || sections[0];
  const hasActiveFilters = selectedCategory !== "All" || skillQuery.trim() !== "";
  const openProjectCount = visibleProjects.filter((project) => project.status === "OPEN").length;
  const sectionDescription =
    activeSection === "mine"
      ? "Projects you created and can manage directly."
      : activeSection === "joined"
        ? "Projects where your request was approved and you are now on the team."
      : activeSection === "applications"
        ? "Requests you already sent to other project owners."
        : "Browse every active collaboration brief in the network.";

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
      <main className="dashboard-page dashboard-explorer min-h-screen bg-ink text-slate-100 antialiased">
      <header className="dashboard-topbar">
        <div className="dashboard-topbar-copy">
          <div>
            <div className="dashboard-brand">CollabMatch</div>
          </div>
          <div className="dashboard-topbar-summary">
            <span className="dashboard-topbar-pill">{activeSectionConfig.label}</span>
            <p>
              {visibleProjects.length} visible
              <span>•</span>
              {openProjectCount} open
            </p>
          </div>
        </div>
        <div className="dashboard-top-actions">
          <button type="button" className="topbar-action-button" onClick={() => setShowProjectForm(true)}>
            Post Project
          </button>
          <button type="button" className="icon-button dashboard-avatar" aria-label="Open profile" onClick={handleOpenProfile}>
            {user.firstname?.[0]}{user.lastname?.[0]}
          </button>
        </div>
      </header>

      <div className="dashboard-layout">
        <aside className="dashboard-sidebar">
          <div>
            <h1 className="sidebar-title">Project Explorer</h1>
            <p className="sidebar-subtitle">Jump between your feed, your posts, and your outgoing applications.</p>
          </div>

          <div className="sidebar-nav">
            {sections.map((section) => {
              const count =
                section.id === "mine"
                  ? projects.filter((project) => project.owner).length
                  : section.id === "joined"
                    ? projects.filter((project) => project.joined).length
                  : section.id === "applications"
                    ? projects.filter((project) => project.joinRequested).length
                    : projects.length;

              return (
                <button
                  key={section.id}
                  type="button"
                  className={`sidebar-link ${activeSection === section.id ? "is-active" : ""}`}
                  onClick={() => setActiveSection(section.id)}
                >
                  <span>{section.id === "all" ? "▦" : section.id === "mine" ? "✎" : section.id === "joined" ? "◉" : "☑"}</span>
                  {section.label}
                  <strong className="sidebar-link-count">{count}</strong>
                </button>
              );
            })}
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

            <label className="filter-label">
              Skills Required
              <input
                type="search"
                className="filter-input"
                value={skillQuery}
                onChange={(event) => setSkillQuery(event.target.value)}
                placeholder="Search skills, roles, owner, or keywords"
              />
            </label>

            {hasActiveFilters ? (
              <button
                type="button"
                className="sidebar-footer-link"
                onClick={() => {
                  setSelectedCategory("All");
                  setSkillQuery("");
                }}
              >
                <span>✕</span>
                Clear Filters
              </button>
            ) : null}
          </section>

          <div className="sidebar-footer-links">
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
              {activeSection === "mine"
                ? "Own the"
                : activeSection === "joined"
                  ? "Work with the"
                  : activeSection === "applications"
                    ? "Track your"
                    : "Discover the"}
              <span className="dashboard-hero-accent">
                {" "}
                {activeSection === "mine"
                  ? "Build."
                  : activeSection === "joined"
                    ? "Team."
                    : activeSection === "applications"
                      ? "Pipeline."
                      : "Future."}
              </span>
            </h2>
            <p>{sectionDescription}</p>
          </section>

          {error ? <p className="dashboard-inline-error">{error}</p> : null}

          <section className="dashboard-card-grid">
            {visibleProjects.map((project, index) => (
              <article key={project.id} className="explorer-card">
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
                  <button
                    type="button"
                    className="explorer-secondary-button"
                    onClick={() => handleOpenProject(project.id)}
                  >
                    View Detail
                  </button>
                  <button
                    type="button"
                    className="explorer-primary-button"
                    disabled={!project.owner && !project.joined && (project.joinRequested || joiningProjectId === project.id)}
                    onClick={() => (project.owner || project.joined ? handleOpenProject(project.id) : handleJoinProject(project.id))}
                  >
                    {project.owner
                      ? "Manage"
                      : project.joined
                        ? "Open Team"
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

            {visibleProjects.length === 0 ? (
              <article className="explorer-empty-state">
                <h3>
                  {hasActiveFilters
                    ? "No projects match these filters"
                    : activeSection === "mine"
                      ? "You have not posted any projects yet"
                      : activeSection === "joined"
                        ? "You have not joined any projects yet"
                      : activeSection === "applications"
                        ? "You have not applied to any projects yet"
                        : "No projects yet"}
                </h3>
                <p>
                  {hasActiveFilters
                    ? "Clear the active category or skill filter to see the rest of your project feed."
                    : activeSection === "mine"
                      ? "Create a project and it will appear here with owner controls."
                      : activeSection === "joined"
                        ? "Once an owner approves your request, the project will appear here."
                      : activeSection === "applications"
                        ? "Join a project from the main feed and it will show up here."
                        : "Create the first project post and it will appear here once saved to the database."}
                </p>
                <button
                  type="button"
                  className="explorer-primary-button"
                  onClick={() => {
                    if (hasActiveFilters) {
                      setSelectedCategory("All");
                      setSkillQuery("");
                      return;
                    }
                    if (activeSection === "applications" || activeSection === "joined") {
                      setActiveSection("all");
                      return;
                    }
                    setActiveSection("all");
                    setShowProjectForm(true);
                  }}
                >
                  {hasActiveFilters ? "Clear Filters" : activeSection === "applications" || activeSection === "joined" ? "Browse Projects" : "Post a Project"}
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
                  rows={5}
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
