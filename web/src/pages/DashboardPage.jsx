import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { fetchCurrentUser, logoutUser } from "../services/api";

const featuredProjects = [
  {
    title: "EduSync Study Hub",
    badge: "Academic",
    description: "Build a student-focused collaboration space for shared notes, schedules, and peer support.",
    tags: ["UX Design", "React", "Research"],
    seats: 4
  },
  {
    title: "Barangay Connect",
    badge: "Community",
    description: "Create a lightweight platform that helps local teams organize outreach drives and volunteer work.",
    tags: ["Project Lead", "Backend", "Content"],
    seats: 3
  },
  {
    title: "Creator Sprint",
    badge: "Trending",
    description: "Launch a cross-discipline media campaign with designers, writers, and video editors in one team.",
    tags: ["Branding", "Video", "Social"],
    seats: 4
  }
];

const howItWorks = [
  {
    title: "Create Profile",
    description: "Highlight your skills, interests, and preferred project roles so teams can find you."
  },
  {
    title: "Find Projects",
    description: "Browse active listings, compare opportunities, and shortlist the projects that match."
  },
  {
    title: "Collaborate",
    description: "Join a team, connect with collaborators, and move from planning to building faster."
  }
];

function DashboardPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("collabmatch_access_token") || localStorage.getItem("collabmatch_token");

    if (!token) {
      setLoading(false);
      return;
    }

    fetchCurrentUser(token)
      .then(setUser)
      .catch(() => {
        localStorage.removeItem("collabmatch_access_token");
        localStorage.removeItem("collabmatch_refresh_token");
        localStorage.removeItem("collabmatch_token");
      })
      .finally(() => setLoading(false));
  }, []);

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

  if (loading) {
    return <main className="dashboard-page"><section className="card"><p>Loading dashboard...</p></section></main>;
  }

  const isAuthenticated = Boolean(user);

  function handleBrowseProjects() {
    navigate(isAuthenticated ? "/projects" : "/login");
  }

  return (
    <main className="home-page">
      <section className="home-shell">
        <header className="home-header">
          <Link className="brand" to="/">
            CollabMatch
          </Link>
          <nav className="home-nav" aria-label="Dashboard">
            <Link to="/">Home</Link>
            <Link to={isAuthenticated ? "/projects" : "/login"}>Find Projects</Link>
            <a href="#featured-projects">Highlights</a>
            {isAuthenticated ? (
              <>
                <span className="dashboard-user-badge">{user.firstname}</span>
                <button className="nav-logout-button" type="button" onClick={handleLogout}>
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link className="nav-link-button nav-link-ghost" to="/login">
                  Log In
                </Link>
                <Link className="nav-link-button" to="/register">
                  Sign Up
                </Link>
              </>
            )}
          </nav>
        </header>

        <section className="hero-section">
          <div className="hero-copy">
            <span className="eyebrow">{isAuthenticated ? `Welcome back, ${user.firstname}` : "Team up smarter"}</span>
            <h1>Find your ideal project and team in one place.</h1>
            <p>
              {isAuthenticated
                ? "Your dashboard gives you quick access to featured project opportunities, collaboration steps, and the best place to start exploring right after login."
                : "CollabMatch helps students and creators discover trending projects, showcase skills, and join collaboration-ready teams with confidence."}
            </p>
            <div className="hero-actions">
              <button type="button" onClick={handleBrowseProjects}>
                {isAuthenticated ? "Browse Projects" : "Log In"}
              </button>
              <button
                className="button-secondary"
                type="button"
                onClick={() => document.getElementById("featured-projects")?.scrollIntoView({ behavior: "smooth" })}
              >
                {isAuthenticated ? "View Highlights" : "Browse"}
              </button>
            </div>
            <div className="hero-metrics">
              <div>
                <strong>120+</strong>
                <span>Active collaborations</span>
              </div>
              <div>
                <strong>45</strong>
                <span>Open team slots</span>
              </div>
              <div>
                <strong>4.9/5</strong>
                <span>Student satisfaction</span>
              </div>
            </div>
          </div>

          <div className="hero-visual card-surface" aria-label="Collaboration preview">
            <div className="hero-window-bar">
              <span />
              <span />
              <span />
            </div>
            <div className="hero-board">
              <div className="hero-board-main">
                <div className="hero-image-placeholder">
                  <span>Project Team Workspace</span>
                </div>
              </div>
              <div className="hero-board-side">
                <div className="mini-stat">
                  <strong>Ready to explore</strong>
                  <span>See the newest project listings curated for collaboration.</span>
                </div>
                <div className="mini-stat">
                  <strong>{isAuthenticated ? "Profile active" : "Get started"}</strong>
                  <span>{isAuthenticated ? user.email : "Log in to browse project listings and join teams."}</span>
                </div>
                <div className="mini-members" aria-hidden="true">
                  <span />
                  <span />
                  <span />
                  <span />
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="content-section" id="featured-projects">
          <div className="section-heading">
            <span className="eyebrow">Featured Projects</span>
            <h2>Trending projects ready for new collaborators.</h2>
          </div>

          <div className="projects-grid">
            {featuredProjects.map((project) => (
              <article className="project-card card-surface" key={project.title}>
                <div className="project-card-header">
                  <h3>{project.title}</h3>
                  <span className="project-badge">{project.badge}</span>
                </div>
                <p>{project.description}</p>
                <div className="project-tags">
                  {project.tags.map((tag) => (
                    <span key={tag}>{tag}</span>
                  ))}
                </div>
                <div className="project-members" aria-label={`${project.seats} available seats`}>
                  {Array.from({ length: project.seats }).map((_, index) => (
                    <span key={`${project.title}-${index}`} />
                  ))}
                </div>
                <div className="project-actions">
                  <Link className="action-link action-link-primary" to={isAuthenticated ? "/projects" : "/login"}>
                    Join
                  </Link>
                  <Link className="action-link" to={isAuthenticated ? "/projects" : "/login"}>
                    View
                  </Link>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="content-section" id="how-it-works">
          <div className="section-heading">
            <span className="eyebrow">How It Works</span>
            <h2>Simple steps to start collaborating.</h2>
          </div>

          <div className="steps-grid">
            {howItWorks.map((step, index) => (
              <article className="step-card" key={step.title}>
                <div className="step-icon" aria-hidden="true">
                  {index + 1}
                </div>
                <h3>{step.title}</h3>
                <p>{step.description}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="cta-section" id="collaborate">
          <div>
            <span className="eyebrow">Ready to Collaborate</span>
            <h2>Move from dashboard to discovery in one tap.</h2>
          </div>
          <button type="button" onClick={handleBrowseProjects}>
            {isAuthenticated ? "Find Projects" : "Log In to Start"}
          </button>
        </section>
      </section>
    </main>
  );
}

export default DashboardPage;
