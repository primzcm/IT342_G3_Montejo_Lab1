package edu.cit.montejo.collabmatch.project;

import java.time.Instant;
import java.util.List;

public class ProjectDetailResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final String rolesNeeded;
    private final List<String> requiredSkills;
    private final String status;
    private final Instant createdAt;
    private final Long ownerId;
    private final String ownerName;
    private final boolean owner;
    private final boolean joined;
    private final boolean joinRequested;
    private final List<ProjectMemberResponse> members;

    public ProjectDetailResponse(
            Long id,
            String title,
            String description,
            String category,
            String rolesNeeded,
            List<String> requiredSkills,
            String status,
            Instant createdAt,
            Long ownerId,
            String ownerName,
            boolean owner,
            boolean joined,
            boolean joinRequested,
            List<ProjectMemberResponse> members
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.rolesNeeded = rolesNeeded;
        this.requiredSkills = requiredSkills;
        this.status = status;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.owner = owner;
        this.joined = joined;
        this.joinRequested = joinRequested;
        this.members = members;
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

    public List<String> getRequiredSkills() {
        return requiredSkills;
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

    public boolean isJoined() {
        return joined;
    }

    public boolean isJoinRequested() {
        return joinRequested;
    }

    public List<ProjectMemberResponse> getMembers() {
        return members;
    }
}
