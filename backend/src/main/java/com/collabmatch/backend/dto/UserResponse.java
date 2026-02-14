package com.collabmatch.backend.dto;

import java.time.Instant;

public class UserResponse {
    private final String id;
    private final String username;
    private final String email;
    private final Instant createdAt;

    public UserResponse(String id, String username, String email, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
