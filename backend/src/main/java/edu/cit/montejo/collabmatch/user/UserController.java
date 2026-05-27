package edu.cit.montejo.collabmatch.user;

import edu.cit.montejo.collabmatch.common.api.ApiResponse;
import edu.cit.montejo.collabmatch.auth.AuthService;
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
