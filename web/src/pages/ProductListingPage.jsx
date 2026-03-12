import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { fetchCurrentUser, logoutUser } from "../services/api";

const projects = [
  {
    id: 1,
    title: "Capstone Matchboard",
    category: "Web App",
    description: "Build a matching platform that connects students with ideal teammates based on skills and interests.",
    skills: ["React", "UI/UX", "Spring Boot"],
    status: "Open",
    timeMinutes: 110,
    teamSize: 5,
    joined: 3,
    requests: 12
  },
  {
    id: 2,
    title: "Campus Media Hub",
    category: "Content",
    description: "Coordinate articles, graphics, and short-form video assets for a student media collaboration hub.",
    skills: ["Writing", "Design", "Editing"],
    status: "Open",
    timeMinutes: 95,
    teamSize: 5,
    joined: 3,
    requests: 8
  },
  {
    id: 3,
    title: "Barangay Connect",
    category: "Community",
    description: "Launch a digital volunteer coordination board with team roles, schedules, and event tracking.",
    skills: ["Planning", "Backend", "Research"],
    status: "New",
    timeMinutes: 130,
    teamSize: 6,
    joined: 3,
    requests: 10
  },
  {
    id: 4,
    title: "Creator Sprint",
    category: "Marketing",
    description: "Bring together designers, copywriters, and content editors for a collaborative campaign sprint.",
    skills: ["Branding", "Copy", "Video"],
    status: "Open",
    timeMinutes: 100,
    teamSize: 5,
    joined: 4,
    requests: 14
  },
  {
    id: 5,
    title: "Study Circle Finder",
    category: "Academic",
    description: "Design a lightweight system for study group discovery, mentorship, and shared accountability.",
    skills: ["Research", "Frontend", "Testing"],
    status: "In Review",
    timeMinutes: 80,
    teamSize: 4,
    joined: 2,
    requests: 6
  },
  {
    id: 6,
    title: "EventOps Planner",
    category: "Operations",
    description: "Create planning flows for event roles, schedules, and quick updates across student organizations.",
    skills: ["Coordination", "Docs", "Support"],
    status: "Open",
    timeMinutes: 120,
    teamSize: 5,
    joined: 3,
    requests: 9
  }
];

const categories = ["All", "Academic", "Community", "Content", "Marketing", "Operations", "Web App"];
const skillFilters = ["React", "UI/UX", "Spring Boot", "Writing", "Design", "Research", "Planning", "Video"];
const statusFilters = ["Open", "New", "In Review"];

function ProductListingPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("All");
  const [selectedSkills, setSelectedSkills] = useState([]);
  const [selectedStatuses, setSelectedStatuses] = useState([]);
  const [maxMinutes, setMaxMinutes] = useState(140);
  const [sortBy, setSortBy] = useState("title");

  useEffect(() => {
    const token = localStorage.getItem("collabmatch_access_token") || localStorage.getItem("collabmatch_token");

    if (!token) {
      navigate("/login", { replace: true });
      return;
    }

    fetchCurrentUser(token)
      .then(setUser)
      .catch((err) => {
        setError(err.message);
        localStorage.removeItem("collabmatch_access_token");
        localStorage.removeItem("collabmatch_refresh_token");
        localStorage.removeItem("collabmatch_token");
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  async function handleLogout() {
    const accessToken = localStorage.getItem("collabmatch_access_token") || localStorage.getItem("collabmatch_token");
    const refreshToken = localStorage.getItem("collabmatch_refresh_token");

    try {
      if (accessToken && refreshToken) {
        await logoutUser({ accessToken, refreshToken });
      }
    } catch {
      // Ignore logout errors because local token removal is enough for the client.
    } finally {
      localStorage.removeItem("collabmatch_access_token");
      localStorage.removeItem("collabmatch_refresh_token");
      localStorage.removeItem("collabmatch_token");
      navigate("/login", { replace: true });
    }
  }

  function toggleValue(value, selected, setter) {
    setter(selected.includes(value) ? selected.filter((item) => item !== value) : [...selected, value]);
  }

  const filteredProjects = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();

    return [...projects]
      .filter((project) => category === "All" || project.category === category)
      .filter((project) => project.timeMinutes <= maxMinutes)
      .filter((project) => selectedSkills.length === 0 || selectedSkills.some((skill) => project.skills.includes(skill)))
      .filter((project) => selectedStatuses.length === 0 || selectedStatuses.includes(project.status))
      .filter((project) => {
        if (!normalizedSearch) {
          return true;
        }

        return [project.title, project.description, project.category, ...project.skills]
          .join(" ")
          .toLowerCase()
          .includes(normalizedSearch);
      })
      .sort((left, right) => {
        if (sortBy === "team") {
          return right.teamSize - left.teamSize;
        }

        if (sortBy === "time") {
          return left.timeMinutes - right.timeMinutes;
        }

        return left.title.localeCompare(right.title);
      });
  }, [category, maxMinutes, search, selectedSkills, selectedStatuses, sortBy]);

  if (loading) {
    return <main className="dashboard-page"><section className="card"><p>Loading projects...</p></section></main>;
  }

  if (error || !user) {
    return (
      <main className="dashboard-page">
        <section className="card">
          <p className="error">{error || "Unable to load projects."}</p>
          <button type="button" onClick={() => navigate("/login", { replace: true })}>
            Back to login
          </button>
        </section>
      </main>
    );
  }

  return (
    <main className="listing-page">
      <section className="listing-shell">
        <header className="listing-header card-surface">
          <div className="listing-brand-row">
            <Link className="brand" to="/">
              CollabMatch
            </Link>
            <nav className="listing-nav" aria-label="Projects">
              <Link to="/">Home</Link>
              <Link to="/projects">Find Projects</Link>
              <a href="#listing-content">Post Project</a>
              <a href="#listing-content">Headline</a>
            </nav>
          </div>

          <div className="listing-toolbar">
            <label className="listing-search compact-search">
              <span className="sr-only">Search projects</span>
              <input
                type="search"
                placeholder="Search"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
            </label>
            <div className="listing-profile">
              <Link className="nav-link-button listing-dashboard-button" to="/">
                Dashboard
              </Link>
              <span>{user.firstname} Profile</span>
              <button className="nav-logout-button" type="button" onClick={handleLogout}>
                Logout
              </button>
            </div>
          </div>
        </header>

        <section className="listing-title-row">
          <div>
            <h1>Headline: Project Listing</h1>
            <p>Browse project opportunities, compare teams, and filter listings based on your strengths.</p>
          </div>
        </section>

        <section className="listing-content" id="listing-content">
          <aside className="listing-sidebar card-surface">
            <div className="filter-group">
              <h2>Category</h2>
              {categories.map((item) => (
                <label className="filter-option" key={item}>
                  <input
                    type="radio"
                    name="category"
                    checked={category === item}
                    onChange={() => setCategory(item)}
                  />
                  <span>{item}</span>
                </label>
              ))}
            </div>

            <div className="filter-group">
              <h2>Skills Needed</h2>
              {skillFilters.map((item) => (
                <label className="filter-option" key={item}>
                  <input
                    type="checkbox"
                    checked={selectedSkills.includes(item)}
                    onChange={() => toggleValue(item, selectedSkills, setSelectedSkills)}
                  />
                  <span>{item}</span>
                </label>
              ))}
            </div>

            <div className="filter-group">
              <h2>Time Commitment</h2>
              <input
                className="listing-range"
                type="range"
                min="60"
                max="140"
                step="10"
                value={maxMinutes}
                onChange={(event) => setMaxMinutes(Number(event.target.value))}
              />
              <p>Up to {maxMinutes} minutes</p>
            </div>

            <div className="filter-group">
              <h2>Status</h2>
              {statusFilters.map((item) => (
                <label className="filter-option" key={item}>
                  <input
                    type="checkbox"
                    checked={selectedStatuses.includes(item)}
                    onChange={() => toggleValue(item, selectedStatuses, setSelectedStatuses)}
                  />
                  <span>{item}</span>
                </label>
              ))}
            </div>
          </aside>

          <section className="listing-main card-surface">
            <div className="listing-controls">
              <label className="listing-search">
                <span className="sr-only">Search listing content</span>
                <input
                  type="search"
                  placeholder="Search"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />
              </label>

              <label className="listing-sort">
                <span>Sort by:</span>
                <select value={sortBy} onChange={(event) => setSortBy(event.target.value)}>
                  <option value="title">Headline</option>
                  <option value="team">Team size</option>
                  <option value="time">Time</option>
                </select>
              </label>
            </div>

            <div className="listing-grid">
              {filteredProjects.map((project) => (
                <article className="listing-card" key={project.id}>
                  <div className="listing-card-header">
                    <div>
                      <h3>{project.title}</h3>
                      <p>{project.description}</p>
                    </div>
                    <span className="project-badge">{project.category}</span>
                  </div>

                  <div className="listing-meta">
                    <span><strong>Skills</strong> {project.skills.join(" / ")}</span>
                    <span><strong>Status</strong> {project.status}</span>
                    <span><strong>Time</strong> {project.timeMinutes} min</span>
                    <span><strong>Team</strong> {project.teamSize}</span>
                  </div>

                  <div className="listing-team-row">
                    <div className="mini-members" aria-hidden="true">
                      {Array.from({ length: project.teamSize - 1 }).map((_, index) => (
                        <span key={`${project.id}-${index}`} />
                      ))}
                    </div>
                    <span className="listing-team-count">Team {project.joined}/{project.teamSize}</span>
                  </div>

                  <div className="listing-card-actions">
                    <button className="button-secondary" type="button">View Detail</button>
                    <button type="button">Join ({project.requests} requests)</button>
                  </div>
                </article>
              ))}
            </div>

            {filteredProjects.length === 0 && (
              <div className="listing-empty">
                <p>No projects match your current filters.</p>
              </div>
            )}

            <div className="listing-pagination" aria-label="Pagination">
              <button className="button-secondary" type="button">{"<<"}</button>
              <button className="button-secondary" type="button">{"<"}</button>
              <span className="listing-page-number active">1</span>
              <span className="listing-page-number">2</span>
              <span className="listing-page-number">3</span>
              <button className="button-secondary" type="button">{">"}</button>
              <button className="button-secondary" type="button">{">>"}</button>
            </div>
          </section>
        </section>

        <footer className="listing-footer card-surface">
          <div>
            <h3>Page Links</h3>
            <a href="#listing-content">Headline</a>
            <Link to="/">Home</Link>
            <Link to="/projects">Projects</Link>
          </div>
          <div>
            <h3>About</h3>
            <a href="#listing-content">Company</a>
            <a href="#listing-content">Mission</a>
            <a href="#listing-content">Careers</a>
          </div>
          <div>
            <h3>Support</h3>
            <a href="#listing-content">Help Center</a>
            <a href="#listing-content">Guides</a>
            <a href="#listing-content">Privacy</a>
          </div>
          <div>
            <h3>Contact</h3>
            <a href="mailto:hello@collabmatch.app">hello@collabmatch.app</a>
            <a href="#listing-content">Feedback</a>
            <a href="#listing-content">Updates</a>
          </div>
          <div>
            <h3>Social Icons</h3>
            <span className="footer-socials">
              <span>f</span>
              <span>x</span>
              <span>in</span>
              <span>ig</span>
            </span>
            <p>Copyright</p>
          </div>
        </footer>
      </section>
    </main>
  );
}

export default ProductListingPage;
