package com.collabmatch.backend.controller;

import com.collabmatch.backend.dto.UserResponse;
import com.collabmatch.backend.model.User;
import com.collabmatch.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(authService.toUserResponse(user));
    }
}
