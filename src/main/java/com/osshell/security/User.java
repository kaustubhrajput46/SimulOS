package com.osshell.security;

import java.util.Objects;

/**
 * Represents a user in the system.
 */
public class User {
    private final String username;
    private final String passwordHash; // In a real system, use bcrypt. Here we simulate.
    private final Role role;

    public User(String username, String password, Role role) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public boolean verifyPassword(String password) {
        return Objects.equals(this.passwordHash, hashPassword(password));
    }

    private String hashPassword(String password) {
        // Simple simulation of hashing
        return Integer.toHexString(password.hashCode());
    }
}

