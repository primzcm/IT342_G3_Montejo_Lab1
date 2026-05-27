package edu.cit.montejo.collabmatch.service;

import edu.cit.montejo.collabmatch.dto.UpdateUserProfileRequest;
import edu.cit.montejo.collabmatch.dto.UserResponse;
import edu.cit.montejo.collabmatch.exception.ForbiddenException;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserService userService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User("klein", "klein@example.com", "Klein", "Moretti", "USER", "hash", Instant.now());
        ReflectionTestUtils.setField(currentUser, "id", 9L);
    }

    @Test
    void updateUserProfileNormalizesOptionalFields() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstname("Klein");
        request.setLastname("Moretti");
        request.setBio("   ");
        request.setSkills(" Kotlin, Compose ");

        UserResponse response = new UserResponse(
                9L,
                "klein",
                "klein@example.com",
                "Klein",
                "Moretti",
                "USER",
                currentUser.getCreatedAt(),
                null,
                "Kotlin, Compose"
        );

        when(userRepository.findById(9L)).thenReturn(Optional.of(currentUser));
        when(authService.toUserResponse(currentUser)).thenReturn(response);

        UserResponse updated = userService.updateUserProfile(currentUser, 9L, request);

        assertEquals("Klein", updated.getFirstname());
        assertNull(currentUser.getBio());
        assertEquals("Kotlin, Compose", currentUser.getSkills());
    }

    @Test
    void updateUserProfileRejectsDifferentUser() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstname("Klein");
        request.setLastname("Moretti");

        assertThrows(ForbiddenException.class, () -> userService.updateUserProfile(currentUser, 99L, request));
    }
}
