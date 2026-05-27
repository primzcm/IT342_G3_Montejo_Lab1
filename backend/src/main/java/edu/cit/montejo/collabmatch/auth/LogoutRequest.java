package edu.cit.montejo.collabmatch.auth;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {
    @NotBlank(message = "refreshToken is required")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
