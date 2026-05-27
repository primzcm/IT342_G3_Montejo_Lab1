package edu.cit.montejo.collabmatch.service;

import edu.cit.montejo.collabmatch.dto.UpdateUserProfileRequest;
import edu.cit.montejo.collabmatch.dto.UserResponse;
import edu.cit.montejo.collabmatch.exception.ForbiddenException;
import edu.cit.montejo.collabmatch.exception.NotFoundException;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthService authService;

    public UserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long userId) {
        return authService.toUserResponse(getUserOrThrow(userId));
    }

    @Transactional
    public UserResponse updateUserProfile(User currentUser, Long userId, UpdateUserProfileRequest request) {
        if (!currentUser.getId().equals(userId)) {
            throw new ForbiddenException("You can only update your own profile");
        }

        User user = getUserOrThrow(userId);
        user.updateProfile(
                request.getFirstname().trim(),
                request.getLastname().trim(),
                normalizeOptionalText(request.getBio()),
                normalizeOptionalText(request.getSkills())
        );

        return authService.toUserResponse(user);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
