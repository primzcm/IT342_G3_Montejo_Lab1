package edu.cit.montejo.collabmatch.dto;

import java.time.Instant;

public class UserResponse {
    private final Long id;
    private final String email;
    private final String firstname;
    private final String lastname;
    private final String role;
    private final Instant createdAt;

    public UserResponse(Long id, String email, String firstname, String lastname, String role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
