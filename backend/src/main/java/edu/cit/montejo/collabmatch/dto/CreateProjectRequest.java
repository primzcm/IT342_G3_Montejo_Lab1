package edu.cit.montejo.collabmatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateProjectRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must be at most 5000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 80, message = "Category must be at most 80 characters")
    private String category;

    @NotBlank(message = "Roles needed is required")
    @Size(max = 5000, message = "Roles needed must be at most 5000 characters")
    private String rolesNeeded;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRolesNeeded() {
        return rolesNeeded;
    }

    public void setRolesNeeded(String rolesNeeded) {
        this.rolesNeeded = rolesNeeded;
    }
}
