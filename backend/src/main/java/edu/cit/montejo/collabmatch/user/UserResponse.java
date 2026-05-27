package edu.cit.montejo.collabmatch.user;

import java.time.Instant;

public class UserResponse {
    private final Long id;
    private final String username;
    private final String email;
    private final String firstname;
    private final String lastname;
    private final String role;
    private final Instant createdAt;
    private final String bio;
    private final String skills;

    public UserResponse(
            Long id,
            String username,
            String email,
            String firstname,
            String lastname,
            String role,
            Instant createdAt,
            String bio,
            String skills
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
        this.createdAt = createdAt;
        this.bio = bio;
        this.skills = skills;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
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

    public String getBio() {
        return bio;
    }

    public String getSkills() {
        return skills;
    }
}
