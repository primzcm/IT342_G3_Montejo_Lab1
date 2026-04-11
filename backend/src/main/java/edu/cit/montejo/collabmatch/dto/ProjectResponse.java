package edu.cit.montejo.collabmatch.dto;

import java.time.Instant;

public class ProjectResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final String rolesNeeded;
    private final String status;
    private final Instant createdAt;
    private final Long ownerId;
    private final String ownerName;
    private final boolean owner;
    private final boolean joinRequested;

    public ProjectResponse(
            Long id,
            String title,
            String description,
            String category,
            String rolesNeeded,
            String status,
            Instant createdAt,
            Long ownerId,
            String ownerName,
            boolean owner,
            boolean joinRequested
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.rolesNeeded = rolesNeeded;
        this.status = status;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.owner = owner;
        this.joinRequested = joinRequested;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getRolesNeeded() {
        return rolesNeeded;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isJoinRequested() {
        return joinRequested;
    }
}
