package com.osshell.security;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages user accounts and authentication.
 */
public class UserManager {
    private final Map<String, User> users;

    public UserManager() {
        this.users = new HashMap<>();
        // Seed users
        registerUser("admin", "admin123", Role.ADMIN);
        registerUser("user1", "password", Role.USER);
        registerUser("user2", "password", Role.USER);
    }

    public void registerUser(String username, String password, Role role) {
        users.put(username, new User(username, password, role));
    }

    public User authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.verifyPassword(password)) {
            return user;
        }
        return null;
    }
}

