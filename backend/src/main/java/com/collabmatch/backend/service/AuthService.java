package com.collabmatch.backend.service;

import com.collabmatch.backend.dto.AuthResponse;
import com.collabmatch.backend.dto.LoginRequest;
import com.collabmatch.backend.dto.RegisterRequest;
import com.collabmatch.backend.dto.UserResponse;
import com.collabmatch.backend.model.User;
import com.collabmatch.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Instant.now()
        );

        userRepository.save(user);
        String token = createToken(user.getId());

        return new AuthResponse(token, toUserResponse(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = createToken(user.getId());
        return new AuthResponse(token, toUserResponse(user));
    }

    public Optional<User> getUserByToken(String token) {
        Long userId = tokenToUserId.get(token);
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    public void logout(String token) {
        tokenToUserId.remove(token);
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }

    private String createToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        return token;
    }
}
