package edu.cit.montejo.collabmatch.project;

import java.time.Instant;

public class ProjectMessageResponse {
    private final Long id;
    private final Long projectId;
    private final Long authorId;
    private final String authorName;
    private final String content;
    private final Instant createdAt;

    public ProjectMessageResponse(
            Long id,
            Long projectId,
            Long authorId,
            String authorName,
            String content,
            Instant createdAt
    ) {
        this.id = id;
        this.projectId = projectId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
