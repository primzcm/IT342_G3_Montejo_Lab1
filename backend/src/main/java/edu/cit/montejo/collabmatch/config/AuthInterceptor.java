package edu.cit.montejo.collabmatch.config;

import edu.cit.montejo.collabmatch.common.exception.UnauthorizedException;
import edu.cit.montejo.collabmatch.user.User;
import edu.cit.montejo.collabmatch.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authorizationHeader.substring(7);
        User user = authService.getUserByAccessToken(token).orElse(null);
        if (user == null) {
            throw new UnauthorizedException("Invalid token");
        }

        request.setAttribute("currentUser", user);
        return true;
    }
}
