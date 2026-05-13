package com.fs.srs.service;

import com.fs.srs.domain.User;
import com.fs.srs.exceptions.AuthenticationException;
import com.fs.srs.repository.UserRepository;

/**
 * Single responsibility: verify credentials and return the authenticated user.
 * <p>
 * Depends on {@link UserRepository} (the abstraction), not on the SQLite
 * implementation — this is textbook Dependency Inversion (Session 6).
 */
public class AuthService {

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    /**
     * Verify credentials and return the logged-in user.
     *
     * @throws AuthenticationException if username or password is wrong
     */
    public User login(String username, String password) {
        if (username == null || password == null) {
            throw new AuthenticationException("Username and password are required");
        }
        User user = users.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Unknown user"));
        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException("Incorrect password");
        }
        return user;
    }
}
