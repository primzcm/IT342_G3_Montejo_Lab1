package edu.cit.montejo.collabmatch.dto;

import jakarta.validation.constraints.Size;

public class CreateJoinRequest {
    @Size(max = 1000, message = "Message must be at most 1000 characters")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
