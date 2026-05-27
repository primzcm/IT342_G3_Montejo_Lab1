import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  approveJoinRequest,
  createProjectMessage,
  deleteProject,
  fetchCurrentUser,
  fetchProjectDetail,
  fetchProjectMessages,
  fetchProjectRequests,
  logoutUser,
  rejectJoinRequest,
  requestJoinProject,
  updateProject
} from "../services/api";
import { parseSkills } from "../utils/projectSkills";

function createFormState(project = null) {
  return {
    title: project?.title || "",
    description: project?.description || "",
    category: project?.category || "",
    rolesNeeded: project?.rolesNeeded || "",
    status: project?.status || "OPEN"
  };
}

function ProjectDetailPage() {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const accessToken = localStorage.getItem("collabmatch_access_token") || localStorage.getItem("collabmatch_token");
  const refreshToken = localStorage.getItem("collabmatch_refresh_token");

  const [user, setUser] = useState(null);
  const [project, setProject] = useState(null);
  const [requests, setRequests] = useState([]);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [requestError, setRequestError] = useState("");
  const [messageError, setMessageError] = useState("");
  const [joining, setJoining] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [postingMessage, setPostingMessage] = useState(false);
  const [actingRequestId, setActingRequestId] = useState(null);
  const [showEditForm, setShowEditForm] = useState(false);
  const [editForm, setEditForm] = useState(createFormState());
  const [messageDraft, setMessageDraft] = useState("");

  useEffect(() => {
    if (!accessToken) {
      navigate("/login", { replace: true });
      return;
    }

    async function loadPage() {
      try {
        setLoading(true);
        setError("");
        const currentUser = await fetchCurrentUser(accessToken);
        setUser(currentUser);

        const detail = await fetchProjectDetail(accessToken, projectId);
        setProject(detail);
        setEditForm(createFormState(detail));

        if (detail.owner) {
          const requestList = await fetchProjectRequests(accessToken, projectId);
          setRequests(requestList);
        }

        if (detail.owner || detail.joined) {
          const projectMessages = await fetchProjectMessages(accessToken, projectId);
          setMessages(projectMessages);
        } else {
          setMessages([]);
        }
      } catch (err) {
        if (err.status === 401) {
          localStorage.removeItem("collabmatch_access_token");
          localStorage.removeItem("collabmatch_refresh_token");
          localStorage.removeItem("collabmatch_token");
          navigate("/login", { replace: true });
          return;
        }

        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    loadPage();
  }, [accessToken, navigate, projectId]);

  async function reloadProject(includeRequests = true) {
    const detail = await fetchProjectDetail(accessToken, projectId);
    setProject(detail);
    setEditForm(createFormState(detail));

    if (includeRequests && detail.owner) {
      const requestList = await fetchProjectRequests(accessToken, projectId);
      setRequests(requestList);
    }

    if (detail.owner || detail.joined) {
      const projectMessages = await fetchProjectMessages(accessToken, projectId);
      setMessages(projectMessages);
    } else {
      setMessages([]);
    }
  }

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

  async function handleJoinProject() {
    try {
      setJoining(true);
      setError("");
      await requestJoinProject(accessToken, projectId, { message: "" });
      await reloadProject(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setJoining(false);
    }
  }

  async function handleSaveProject(event) {
    event.preventDefault();

    try {
      setSaving(true);
      setError("");
      await updateProject(accessToken, projectId, {
        ...editForm,
        requiredSkills: parseSkills(editForm.rolesNeeded)
      });
      await reloadProject();
      setShowEditForm(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteProject() {
    if (!window.confirm("Delete this project? This also removes its join requests and members.")) {
      return;
    }

    try {
      setDeleting(true);
      setError("");
      await deleteProject(accessToken, projectId);
      navigate("/dashboard", { replace: true });
    } catch (err) {
      setError(err.message);
      setDeleting(false);
    }
  }

  async function handleRequestDecision(requestId, action) {
    try {
      setActingRequestId(requestId);
      setRequestError("");

      if (action === "approve") {
        await approveJoinRequest(accessToken, requestId);
      } else {
        await rejectJoinRequest(accessToken, requestId);
      }

      await reloadProject();
    } catch (err) {
      setRequestError(err.message);
    } finally {
      setActingRequestId(null);
    }
  }

  async function handlePostMessage(event) {
    event.preventDefault();

    if (!messageDraft.trim()) {
      return;
    }

    try {
      setPostingMessage(true);
      setMessageError("");
      await createProjectMessage(accessToken, projectId, { content: messageDraft.trim() });
      setMessageDraft("");
      const projectMessages = await fetchProjectMessages(accessToken, projectId);
      setMessages(projectMessages);
    } catch (err) {
      setMessageError(err.message);
    } finally {
      setPostingMessage(false);
    }
  }

  function handleOpenProfile() {
    navigate("/profile");
  }

  if (loading) {
    return (
      <main className="dashboard-page">
        <section className="card"><p>Loading project...</p></section>
      </main>
    );
  }

  if (!project || !user) {
    return (
      <main className="dashboard-page">
        <section className="card">
          <p className="error">{error || "Unable to load project."}</p>
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
            <span className="dashboard-topbar-pill">{project.owner ? "Owner View" : "Project Detail"}</span>
            <p>
              {project.status}
              <span>•</span>
              {project.category}
            </p>
          </div>
        </div>
        <div className="dashboard-top-actions">
          <button type="button" className="topbar-action-button" onClick={() => navigate("/dashboard")}>
            Back to Dashboard
          </button>
          <button type="button" className="icon-button dashboard-avatar" aria-label="Open profile" onClick={handleOpenProfile}>
            {user.firstname?.[0]}{user.lastname?.[0]}
          </button>
        </div>
      </header>

      <section className="project-detail-shell">
        <div className="project-detail-header">
          <button type="button" className="detail-back-link" onClick={() => navigate("/dashboard")}>
            ← Back to dashboard
          </button>

          <div className="project-detail-actions">
            <button type="button" className="explorer-secondary-button" onClick={handleLogout}>
              Logout
            </button>
            {project.owner ? (
              <>
                <button
                  type="button"
                  className="explorer-secondary-button"
                  onClick={() => setShowEditForm(true)}
                >
                  Edit Project
                </button>
                <button
                  type="button"
                  className="danger-button"
                  disabled={deleting}
                  onClick={handleDeleteProject}
                >
                  {deleting ? "Deleting..." : "Delete Project"}
                </button>
              </>
            ) : project.joined ? (
              <button
                type="button"
                className="explorer-primary-button"
                onClick={() => window.scrollTo({ top: document.body.scrollHeight, behavior: "smooth" })}
              >
                Joined Project
              </button>
            ) : (
              <button
                type="button"
                className="explorer-primary-button"
                disabled={project.joinRequested || joining || project.status !== "OPEN"}
                onClick={handleJoinProject}
              >
                {project.joinRequested
                  ? "Requested"
                  : joining
                    ? "Joining..."
                    : project.status !== "OPEN"
                      ? "Closed"
                      : "Request to Join"}
              </button>
            )}
          </div>
        </div>

        {error ? <p className="dashboard-inline-error">{error}</p> : null}

        <section className="project-detail-grid">
          <article className="project-summary-card">
            <div className="project-summary-top">
              <span className={`status-pill ${project.status === "OPEN" ? "is-open" : "is-closed"}`}>
                {project.status}
              </span>
              <span className="detail-category-tag">{project.category}</span>
            </div>
            <h1 className="project-detail-title">{project.title}</h1>
            <p className="project-detail-description">{project.description}</p>

            <dl className="project-detail-meta">
              <div>
                <dt>Owner</dt>
                <dd>{project.ownerName}</dd>
              </div>
              <div>
                <dt>Roles Needed</dt>
                <dd>{project.rolesNeeded}</dd>
              </div>
              <div>
                <dt>Created</dt>
                <dd>{new Date(project.createdAt).toLocaleString()}</dd>
              </div>
              <div>
                <dt>Members</dt>
                <dd>{project.members.length}</dd>
              </div>
            </dl>
          </article>

          <article className="detail-panel-card">
            <div className="detail-panel-heading">
              <h2>Project Members</h2>
              <p>Approved collaborators attached to this project.</p>
            </div>

            <div className="member-list">
              {project.members.length > 0 ? (
                project.members.map((member) => (
                  <div key={`${member.userId}-${member.joinedAt}`} className="member-row">
                    <div>
                      <strong>{member.name}</strong>
                      <p>Joined {new Date(member.joinedAt).toLocaleString()}</p>
                    </div>
                  </div>
                ))
              ) : (
                <div className="empty-panel-state">
                  <strong>No members yet</strong>
                  <p>Approve a join request to populate the team roster.</p>
                </div>
              )}
            </div>
          </article>
        </section>

        {!project.owner && project.joined ? (
          <section className="detail-panel-card">
            <div className="detail-panel-heading">
              <h2>Your Membership</h2>
              <p>Your join request was approved. You are now part of this project team.</p>
            </div>
          </section>
        ) : null}

        {project.owner || project.joined ? (
          <section className="detail-panel-card">
            <div className="detail-panel-heading">
              <h2>Project Board</h2>
              <p>Post introductions, coordination notes, or progress updates for the team.</p>
            </div>

            <form className="project-message-form" onSubmit={handlePostMessage}>
              <label>
                New message
                <textarea
                  value={messageDraft}
                  onChange={(event) => setMessageDraft(event.target.value)}
                  rows={4}
                  placeholder="Share an update, ask a question, or introduce yourself to the team."
                  maxLength={2000}
                  required
                />
              </label>
              <div className="project-message-form-footer">
                <span>{messageDraft.trim().length}/2000</span>
                <button type="submit" className="explorer-primary-button" disabled={postingMessage || !messageDraft.trim()}>
                  {postingMessage ? "Posting..." : "Post Message"}
                </button>
              </div>
            </form>

            {messageError ? <p className="dashboard-inline-error">{messageError}</p> : null}

            <div className="project-message-list">
              {messages.length > 0 ? (
                messages.map((message) => (
                  <article key={message.id} className="project-message-card">
                    <div className="project-message-card-top">
                      <strong>{message.authorName}</strong>
                      <span>{new Date(message.createdAt).toLocaleString()}</span>
                    </div>
                    <p>{message.content}</p>
                  </article>
                ))
              ) : (
                <div className="empty-panel-state">
                  <strong>No messages yet</strong>
                  <p>Start the conversation with a short introduction or project update.</p>
                </div>
              )}
            </div>
          </section>
        ) : null}

        {project.owner ? (
          <section className="detail-panel-card detail-requests-card">
            <div className="detail-panel-heading">
              <h2>Join Requests</h2>
              <p>Approve or reject incoming requests from the owner view.</p>
            </div>

            {requestError ? <p className="dashboard-inline-error">{requestError}</p> : null}

            <div className="request-table">
              <div className="request-table-head">
                <span>Requester</span>
                <span>Message</span>
                <span>Status</span>
                <span>Action</span>
              </div>

              {requests.length > 0 ? (
                requests.map((request) => {
                  const pending = request.status === "PENDING";
                  return (
                    <div key={request.id} className="request-row">
                      <span>{request.requesterName}</span>
                      <span>{request.message || "No message provided."}</span>
                      <span className={`request-status request-status-${request.status.toLowerCase()}`}>
                        {request.status}
                      </span>
                      <div className="request-actions">
                        <button
                          type="button"
                          className="mini-action-button is-approve"
                          disabled={!pending || actingRequestId === request.id}
                          onClick={() => handleRequestDecision(request.id, "approve")}
                        >
                          {actingRequestId === request.id ? "Saving..." : "Approve"}
                        </button>
                        <button
                          type="button"
                          className="mini-action-button is-reject"
                          disabled={!pending || actingRequestId === request.id}
                          onClick={() => handleRequestDecision(request.id, "reject")}
                        >
                          Reject
                        </button>
                      </div>
                    </div>
                  );
                })
              ) : (
                <div className="empty-panel-state">
                  <strong>No join requests yet</strong>
                  <p>Requests will appear here as other users apply.</p>
                </div>
              )}
            </div>
          </section>
        ) : null}
      </section>

      {showEditForm ? (
        <div className="project-modal-backdrop" onClick={() => setShowEditForm(false)}>
          <section className="project-modal" onClick={(event) => event.stopPropagation()}>
            <div className="project-modal-header">
              <div>
                <h2>Edit Project</h2>
                <p>Update the collaboration brief, category, roles, and open or close the project.</p>
              </div>
              <button type="button" className="project-modal-close" onClick={() => setShowEditForm(false)}>✕</button>
            </div>

            <form className="project-form" onSubmit={handleSaveProject}>
              <label>
                Project Title
                <input
                  type="text"
                  value={editForm.title}
                  onChange={(event) => setEditForm({ ...editForm, title: event.target.value })}
                  required
                />
              </label>

              <label>
                Category
                <input
                  type="text"
                  value={editForm.category}
                  onChange={(event) => setEditForm({ ...editForm, category: event.target.value })}
                  required
                />
              </label>

              <label>
                Skills / Roles Needed
                <input
                  type="text"
                  value={editForm.rolesNeeded}
                  onChange={(event) => setEditForm({ ...editForm, rolesNeeded: event.target.value })}
                  required
                />
              </label>

              <label>
                Status
                <select
                  className="filter-select"
                  value={editForm.status}
                  onChange={(event) => setEditForm({ ...editForm, status: event.target.value })}
                >
                  <option value="OPEN">OPEN</option>
                  <option value="CLOSED">CLOSED</option>
                </select>
              </label>

              <label>
                Description
                <textarea
                  value={editForm.description}
                  onChange={(event) => setEditForm({ ...editForm, description: event.target.value })}
                  rows={5}
                  required
                />
              </label>

              <div className="project-form-actions">
                <button type="button" className="explorer-secondary-button" onClick={() => setShowEditForm(false)}>
                  Cancel
                </button>
                <button type="submit" className="explorer-primary-button" disabled={saving}>
                  {saving ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </form>
          </section>
        </div>
      ) : null}
    </main>
  );
}

export default ProjectDetailPage;
