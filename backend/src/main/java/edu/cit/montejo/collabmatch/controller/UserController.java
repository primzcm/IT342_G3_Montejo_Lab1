package edu.cit.montejo.collabmatch.controller;

import edu.cit.montejo.collabmatch.dto.ApiResponse;
import edu.cit.montejo.collabmatch.dto.UpdateUserProfileRequest;
import edu.cit.montejo.collabmatch.dto.UserResponse;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.service.AuthService;
import edu.cit.montejo.collabmatch.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final AuthService authService;
    private final UserService userService;

    public UserController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(ApiResponse.success(authService.toUserResponse(user)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserProfileRequest updateUserProfileRequest
    ) {
        User user = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateUserProfile(user, user.getId(), updateUserProfileRequest)
        ));
    }
}
