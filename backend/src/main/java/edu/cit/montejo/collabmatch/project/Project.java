package edu.cit.montejo.collabmatch.project;

import edu.cit.montejo.collabmatch.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "roles_needed", nullable = false, columnDefinition = "TEXT")
    private String rolesNeeded;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionIndex asc")
    private List<ProjectSkill> requiredSkills = new ArrayList<>();

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Project() {}

    public Project(User owner, String title, String description, String category, String rolesNeeded, String status) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.category = category;
        this.rolesNeeded = rolesNeeded;
        this.status = status;
    }

    public void updateDetails(String title, String description, String category, String rolesNeeded, String status) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.rolesNeeded = rolesNeeded;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
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

    public List<ProjectSkill> getRequiredSkills() {
        return requiredSkills;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void replaceRequiredSkills(List<String> skills) {
        requiredSkills.clear();
        for (int index = 0; index < skills.size(); index++) {
            requiredSkills.add(new ProjectSkill(this, skills.get(index), index));
        }
        rolesNeeded = String.join(", ", skills);
    }
}
