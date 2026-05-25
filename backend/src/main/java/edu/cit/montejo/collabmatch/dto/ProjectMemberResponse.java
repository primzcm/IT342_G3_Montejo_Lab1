package edu.cit.montejo.collabmatch.dto;

import java.time.Instant;

public class ProjectMemberResponse {
    private final Long userId;
    private final String name;
    private final Instant joinedAt;

    public ProjectMemberResponse(Long userId, String name, Instant joinedAt) {
        this.userId = userId;
        this.name = name;
        this.joinedAt = joinedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
