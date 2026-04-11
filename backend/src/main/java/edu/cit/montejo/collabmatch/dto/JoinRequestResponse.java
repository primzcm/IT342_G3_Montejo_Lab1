package edu.cit.montejo.collabmatch.dto;

import java.time.Instant;

public class JoinRequestResponse {
    private final Long id;
    private final Long projectId;
    private final Long requesterId;
    private final String status;
    private final String message;
    private final Instant createdAt;

    public JoinRequestResponse(Long id, Long projectId, Long requesterId, String status, String message, Instant createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.requesterId = requesterId;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
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

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
