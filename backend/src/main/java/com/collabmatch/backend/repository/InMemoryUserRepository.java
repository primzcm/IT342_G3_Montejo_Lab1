package com.collabmatch.backend.repository;

import com.collabmatch.backend.model.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository {
    private final Map<String, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, String> userIdsByUsername = new ConcurrentHashMap<>();

    public Optional<User> findByUsername(String username) {
        String userId = userIdsByUsername.get(username.toLowerCase());
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersById.get(userId));
    }

    public Optional<User> findById(String userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    public boolean existsByUsername(String username) {
        return userIdsByUsername.containsKey(username.toLowerCase());
    }

    public User save(User user) {
        usersById.put(user.getId(), user);
        userIdsByUsername.put(user.getUsername().toLowerCase(), user.getId());
        return user;
    }
}
