package edu.cit.montejo.collabmatch.service;

import edu.cit.montejo.collabmatch.dto.AuthResponse;
import edu.cit.montejo.collabmatch.dto.LoginRequest;
import edu.cit.montejo.collabmatch.dto.RegisterRequest;
import edu.cit.montejo.collabmatch.dto.UserResponse;
import edu.cit.montejo.collabmatch.exception.ConflictException;
import edu.cit.montejo.collabmatch.exception.UnauthorizedException;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.model.RefreshToken;
import edu.cit.montejo.collabmatch.repository.RefreshTokenRepository;
import edu.cit.montejo.collabmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenTtlSeconds;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            @Value("${app.jwt.refresh-token-ttl-seconds:2592000}") long refreshTokenTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.jwtService = jwtService;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        String username = generateUniqueUsername(request.getFirstname(), request.getLastname(), request.getEmail());
        User user = new User(
                username,
                request.getEmail(),
                request.getFirstname(),
                request.getLastname(),
                "USER",
                passwordEncoder.encode(request.getPassword()),
                Instant.now()
        );

        userRepository.save(user);
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return new AuthResponse(toUserResponse(user), accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email or password is incorrect"));

        boolean passwordMatches;
        try {
            passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Email or password is incorrect");
        }

        if (!passwordMatches) {
            throw new UnauthorizedException("Email or password is incorrect");
        }

        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = createRefreshToken(user);
        return new AuthResponse(toUserResponse(user), accessToken, refreshToken);
    }

    public Optional<User> getUserByAccessToken(String accessToken) {
        return jwtService.parseUserId(accessToken).flatMap(userRepository::findById);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstname(),
                user.getLastname(),
                user.getRole(),
                user.getCreatedAt(),
                user.getBio(),
                user.getSkills()
        );
    }

    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusSeconds(refreshTokenTtlSeconds);
        refreshTokenRepository.save(new RefreshToken(user, token, expiryDate));
        return token;
    }

    private static final Pattern NON_ALPHANUM = Pattern.compile("[^a-z0-9]");

    private String generateUniqueUsername(String firstname, String lastname, String email) {
        String base = (firstname + "." + lastname).toLowerCase();
        base = NON_ALPHANUM.matcher(base).replaceAll("");
        if (base.isBlank()) {
            base = email.split("@", 2)[0].toLowerCase();
            base = NON_ALPHANUM.matcher(base).replaceAll("");
        }
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }

        String candidate = base;
        for (int attempt = 0; attempt < 20; attempt++) {
            if (!userRepository.existsByUsernameIgnoreCase(candidate)) {
                return candidate;
            }
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            candidate = base + suffix;
            if (candidate.length() > 50) {
                candidate = candidate.substring(0, 50);
            }
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
}
