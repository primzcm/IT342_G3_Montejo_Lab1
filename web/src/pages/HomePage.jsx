import { Link, useNavigate } from "react-router-dom";

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
    description: "Add skills, interests, and the kind of projects you want to join.",
    icon: "◔"
  },
  {
    title: "Find Projects",
    description: "Browse active openings and discover teams that fit your strengths.",
    icon: "⌕"
  },
  {
    title: "Collaborate",
    description: "Connect with teammates, join forces, and build faster together.",
    icon: "◌"
  }
];

function HomePage() {
  const navigate = useNavigate();
  const hasToken = Boolean(localStorage.getItem("collabmatch_access_token") || localStorage.getItem("collabmatch_token"));

  function handlePrimaryAction() {
    navigate(hasToken ? "/dashboard" : "/register");
  }

  return (
    <main className="home-page">
      <section className="home-shell">
        <header className="home-header">
          <Link className="brand" to="/">
            CollabMatch
          </Link>
          <nav className="home-nav" aria-label="Primary">
            <a href="#featured-projects">Projects</a>
            <a href="#how-it-works">How it Works</a>
            <a href="#collaborate">Collaborate</a>
            {hasToken ? (
              <Link className="nav-link-button" to="/dashboard">
                Dashboard
              </Link>
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
            <span className="eyebrow">Team up smarter</span>
            <h1>Find your ideal project and team in one place.</h1>
            <p>
              CollabMatch helps students and creators discover trending projects, showcase skills, and join
              collaboration-ready teams with confidence.
            </p>
            <div className="hero-actions">
              <button type="button" onClick={() => navigate("/login")}>
                Browse
              </button>
              <button className="button-secondary" type="button" onClick={handlePrimaryAction}>
                {hasToken ? "Open Dashboard" : "Create"}
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
                  <span>Team Collaboration Sketch</span>
                </div>
              </div>
              <div className="hero-board-side">
                <div className="mini-stat">
                  <strong>Design Sprint</strong>
                  <span>UI, content, frontend</span>
                </div>
                <div className="mini-stat">
                  <strong>3 open roles</strong>
                  <span>Researcher, dev, editor</span>
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
                  <Link className="action-link action-link-primary" to={hasToken ? "/dashboard" : "/register"}>
                    Join
                  </Link>
                  <Link className="action-link" to={hasToken ? "/dashboard" : "/login"}>
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
            {howItWorks.map((step) => (
              <article className="step-card" key={step.title}>
                <div className="step-icon" aria-hidden="true">
                  {step.icon}
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
            <h2>Start building with the right team today.</h2>
          </div>
          <button type="button" onClick={handlePrimaryAction}>
            Get Started
          </button>
        </section>

        <footer className="home-footer">
          <div>
            <h3>CollabMatch</h3>
            <a href="mailto:hello@collabmatch.app">hello@collabmatch.app</a>
          </div>
          <div>
            <h3>Company</h3>
            <a href="#featured-projects">About</a>
            <a href="#collaborate">Contact</a>
            <a href="#how-it-works">Careers</a>
          </div>
          <div>
            <h3>Explore</h3>
            <a href="#featured-projects">Projects</a>
            <Link to="/login">Login</Link>
            <Link to="/register">Sign Up</Link>
          </div>
          <div>
            <h3>Legal</h3>
            <a href="#collaborate">Terms</a>
            <a href="#collaborate">Privacy</a>
            <span className="footer-socials" aria-label="Social links">
              <span>◼</span>
              <span>◼</span>
              <span>◼</span>
            </span>
          </div>
        </footer>
      </section>
    </main>
  );
}

export default HomePage;
