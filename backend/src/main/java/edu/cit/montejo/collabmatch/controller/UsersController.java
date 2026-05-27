package edu.cit.montejo.collabmatch.controller;

import edu.cit.montejo.collabmatch.dto.ApiResponse;
import edu.cit.montejo.collabmatch.dto.UpdateUserProfileRequest;
import edu.cit.montejo.collabmatch.dto.UserResponse;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {
    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(userId)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            HttpServletRequest request,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserProfileRequest updateUserProfileRequest
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateUserProfile(currentUser, userId, updateUserProfileRequest)
        ));
    }
}
