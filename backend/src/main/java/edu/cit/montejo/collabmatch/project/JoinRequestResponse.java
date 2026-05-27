package edu.cit.montejo.collabmatch.project;

import java.time.Instant;

public class JoinRequestResponse {
    private final Long id;
    private final Long projectId;
    private final Long requesterId;
    private final String requesterName;
    private final String status;
    private final String message;
    private final Instant createdAt;
    private final Instant reviewedAt;

    public JoinRequestResponse(
            Long id,
            Long projectId,
            Long requesterId,
            String requesterName,
            String status,
            String message,
            Instant createdAt,
            Instant reviewedAt
    ) {
        this.id = id;
        this.projectId = projectId;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
