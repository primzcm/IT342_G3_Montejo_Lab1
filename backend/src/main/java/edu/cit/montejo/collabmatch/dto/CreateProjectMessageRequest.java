package edu.cit.montejo.collabmatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateProjectMessageRequest {
    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message must be at most 2000 characters")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
