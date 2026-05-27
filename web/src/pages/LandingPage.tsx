import React from "react";
import { Link } from "react-router-dom";

const featuredProjects = [
  {
    badge: "NEURO ARCHITECTURE",
    title: "Meta-Verse Civic Infrastructure",
    description:
      "Building the first decentralized governance modules for virtual city planning. Seeking senior Solidity devs and UX futurists.",
    members: ["JD", "ML", "+4"]
  },
  {
    badge: "HIGH PRIORITY",
    title: "Autonomous Logistics Mesh",
    description:
      "Redefining last-mile delivery using localized swarm intelligence and drone networks. Looking for AI pathfinding experts.",
    slots: "3",
    timeline: "6 Mo"
  },
  {
    title: "Hardware Synthesizer Module",
    description: "Open-source analog signal processing kit for modern performers.",
    accent: "amber"
  },
  {
    title: "Oceanic Data Viz Platform",
    description: "Real-time mapping of global currents for climate research NGOs.",
    accent: "teal"
  }
];

function LandingPage() {
  const hasToken = Boolean(localStorage.getItem("collabmatch_access_token") || localStorage.getItem("collabmatch_token"));
  const primaryHref = hasToken ? "/dashboard" : "/register";
  const loginHref = hasToken ? "/dashboard" : "/login";

  return (
    <main className="landing-page min-h-screen bg-ink text-slate-100">
      <header className="landing-shell">
        <nav className="landing-nav">
          <div className="brand-mark">CollabMatch</div>
          <div className="landing-nav-links">
            <a href="#trending" className="is-active">Explore</a>
            <a href="#teams">Teams</a>
            <a href="#resources">Resources</a>
          </div>
          <div className="landing-nav-actions">
            <Link to={loginHref} className="landing-text-link">{hasToken ? "Dashboard" : "Login"}</Link>
            <Link to={primaryHref} className="landing-button landing-button-primary">Create Project</Link>
          </div>
        </nav>
      </header>

      <section className="landing-shell hero-section">
        <div className="hero-copy">
          <div className="hero-pill">
            <span className="hero-pill-dot" />
            LIVE COLLABORATION
          </div>
          <h1 className="hero-title">
            Find Your
            <span className="hero-accent"> Creative </span>
            Kinetic.
          </h1>
          <p className="hero-text">
            The elite marketplace for high-performance teams. Match with specialists,
            build asymmetric projects, and catalyze innovation in the midnight hour.
          </p>
          <div className="hero-actions">
            <Link to={primaryHref} className="landing-button landing-button-primary">
              Start Exploring
              <span aria-hidden="true">→</span>
            </Link>
            <a href="#how-it-works" className="landing-button landing-button-secondary">How it works</a>
          </div>
        </div>

        <div className="hero-visual" aria-hidden="true">
          <div className="hero-card hero-card-main">
            <div className="hero-card-prism" />
          </div>
          <div className="hero-card hero-card-floating">
            <div className="hero-card-icon">✦</div>
            <div className="hero-card-line hero-card-line-short" />
            <div className="hero-card-line" />
            <div className="hero-card-line hero-card-line-faint" />
          </div>
        </div>
      </section>

      <section className="landing-section" id="trending">
        <div className="landing-shell section-stack">
          <div className="section-heading">
            <div>
              <h2>Trending Projects</h2>
              <p>Curated opportunities for high-impact specialists to join forces.</p>
            </div>
            <a href="#teams" className="section-link">View All Opportunities →</a>
          </div>

          <div className="project-grid">
            <article className="project-feature-card">
              <div className="project-feature-visual">
                <div className="project-badge">{featuredProjects[0].badge}</div>
                <div className="project-feature-content">
                  <h3>{featuredProjects[0].title}</h3>
                  <p>{featuredProjects[0].description}</p>
                  <div className="project-feature-footer">
                    <div className="member-group">
                      {featuredProjects[0].members.map((member) => (
                        <span key={member} className="member-bubble">{member}</span>
                      ))}
                    </div>
                    <Link to={primaryHref} className="landing-button landing-button-compact">Apply Now</Link>
                  </div>
                </div>
              </div>
            </article>

            <article className="project-side-card">
              <div className="project-badge">{featuredProjects[1].badge}</div>
              <h3>{featuredProjects[1].title}</h3>
              <p>{featuredProjects[1].description}</p>
              <div className="project-metrics">
                <div>
                  <span>OPEN SPOTS</span>
                  <strong>{featuredProjects[1].slots}</strong>
                </div>
                <div>
                  <span>TIMELINE</span>
                  <strong>{featuredProjects[1].timeline}</strong>
                </div>
              </div>
              <Link to={primaryHref} className="landing-button landing-button-secondary project-side-action">
                View Details
              </Link>
            </article>

            {featuredProjects.slice(2).map((project) => (
              <article key={project.title} className="project-mini-card">
                <div className={`project-mini-visual project-mini-${project.accent}`} />
                <div>
                  <h3>{project.title}</h3>
                  <p>{project.description}</p>
                  <Link to={primaryHref} className="project-mini-link">JOIN TEAM →</Link>
                </div>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="landing-section" id="how-it-works">
        <div className="landing-shell">
          <div className="match-card">
            <div className="match-copy">
              <h2>
                Build the Team
                <br />
                You&apos;ve Always
                <span className="hero-accent"> Envisioned.</span>
              </h2>
              <p>
                Our catalyst matching engine uses behavioral data and skill-set synergy
                to place you with the right people at the right time. No more ghosting,
                just pure execution.
              </p>
              <div className="match-stats" id="teams">
                <div>
                  <strong>1.2k+</strong>
                  <span>ACTIVE TEAMS</span>
                </div>
                <div>
                  <strong>98%</strong>
                  <span>MATCH RATE</span>
                </div>
              </div>
            </div>

            <div className="match-visual">
              <div className="match-node match-node-top">
                <span className="match-node-dot amber" />
                <div className="match-node-lines" />
              </div>
              <div className="match-node match-node-middle">
                <span className="match-node-dot blue" />
                <div className="match-node-lines" />
              </div>
              <div className="match-node match-node-right">
                <span className="match-node-dot amber" />
                <div className="match-node-lines" />
              </div>
              <div className="match-ready">
                <span>🤝</span>
                <strong>READY TO MATCH</strong>
              </div>
            </div>
          </div>
        </div>
      </section>

      <footer className="landing-footer" id="resources">
        <div className="landing-shell landing-footer-grid">
          <div>
            <div className="brand-mark">CollabMatch</div>
            <p>Building the future of collective intelligence, one project at a time.</p>
          </div>
          <div className="footer-links">
            <a href="#how-it-works">About</a>
            <a href="#resources">Contact</a>
            <a href="#resources">Terms</a>
            <a href="#resources">Privacy</a>
            <a href="#resources">Careers</a>
          </div>
          <p className="footer-caption">© 2026 CollabMatch. The Midnight Catalyst.</p>
        </div>
      </footer>
    </main>
  );
}

export default LandingPage;
