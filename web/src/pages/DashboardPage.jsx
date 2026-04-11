import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchCurrentUser, logoutUser } from "../services/api";

const projectCards = [
  {
    title: "Solaris Energy Grid",
    description: "Decentralized renewable energy management protocol using blockchain for smart city infrastructure.",
    badge: "HIGH IMPACT",
    mark: "solar",
    icons: ["✦", "◈", "⬢"]
  },
  {
    title: "Aether Neural Interface",
    description: "Next-generation cognitive assistance system leveraging LLMs to bridge the gap between intent and execution.",
    badge: "HIGH IMPACT",
    mark: "neural",
    icons: ["⚙", "◌", "⚗"]
  },
  {
    title: "Zenith FinTech Hub",
    description: "Borderless financial ecosystem designed for micro-entrepreneurs in emerging markets.",
    badge: "",
    mark: "fin",
    icons: ["⌘", "◫", "◎"]
  },
  {
    title: "NeoHealth Diagnostics",
    description: "AI-powered mobile diagnostic tool for early detection of rare dermatological conditions.",
    badge: "",
    mark: "health",
    icons: ["✚", "◔", "◉"]
  },
  {
    title: "Sentinel Web3 Security",
    description: "Real-time smart contract auditing and vulnerability protection for high-value DeFi protocols.",
    badge: "HIGH IMPACT",
    mark: "shield",
    icons: ["⬣", "⌲", "⬡"]
  },
  {
    title: "Nebula Space Logistics",
    description: "AI orbital debris tracking and autonomous satellite collision avoidance software systems.",
    badge: "",
    mark: "nebula",
    icons: ["✺", "⌁", "◐"]
  }
];

function DashboardPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

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
      // Ignore logout errors because client-side token removal is sufficient.
    } finally {
      localStorage.removeItem("collabmatch_access_token");
      localStorage.removeItem("collabmatch_refresh_token");
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
              <div className="filter-select">
                <span>FinTech</span>
                <span>⌄</span>
              </div>
            </label>

            <div className="filter-label">
              Skills Required
              <div className="skill-chips">
                <span className="skill-chip is-active">React</span>
                <span className="skill-chip">Solidity</span>
                <span className="skill-chip">Python</span>
              </div>
            </div>
          </section>

          <div className="sidebar-divider" />

          <button type="button" className="sidebar-post-button">Post a Project</button>

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

          <section className="dashboard-card-grid">
            {projectCards.map((project, index) => (
              <article key={project.title} className="explorer-card">
                <div className={`explorer-logo logo-${project.mark}`}>
                  <span>{project.title.split(" ")[0]}</span>
                </div>
                {project.badge ? <div className="explorer-badge">{project.badge}</div> : null}
                <h3>{project.title}</h3>
                <p>{project.description}</p>

                <div className="explorer-icons">
                  {project.icons.map((icon) => (
                    <span key={icon}>{icon}</span>
                  ))}
                </div>

                <div className="explorer-actions">
                  <button type="button" className="explorer-secondary-button">View Detail</button>
                  <button type="button" className="explorer-primary-button">Join</button>
                </div>

                {index === 5 ? (
                  <div className="explorer-floating-note">14 Developers Active Now</div>
                ) : null}
              </article>
            ))}
          </section>
        </section>
      </div>
    </main>
  );
}

export default DashboardPage;
