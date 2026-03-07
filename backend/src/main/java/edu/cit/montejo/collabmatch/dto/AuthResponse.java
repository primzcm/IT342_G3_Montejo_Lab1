package edu.cit.montejo.collabmatch.dto;

public class AuthResponse {
    private final UserResponse user;
    private final String accessToken;
    private final String refreshToken;

    public AuthResponse(UserResponse user, String accessToken, String refreshToken) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public UserResponse getUser() {
        return user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
