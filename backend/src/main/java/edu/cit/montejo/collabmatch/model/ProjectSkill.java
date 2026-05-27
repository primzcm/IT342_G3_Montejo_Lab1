package edu.cit.montejo.collabmatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_skills")
public class ProjectSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "skill_name", nullable = false, length = 120)
    private String skillName;

    @Column(name = "position_index", nullable = false)
    private int positionIndex;

    protected ProjectSkill() {}

    public ProjectSkill(Project project, String skillName, int positionIndex) {
        this.project = project;
        this.skillName = skillName;
        this.positionIndex = positionIndex;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getSkillName() {
        return skillName;
    }

    public int getPositionIndex() {
        return positionIndex;
    }
}
