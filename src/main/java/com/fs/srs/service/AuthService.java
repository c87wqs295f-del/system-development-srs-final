package com.fs.srs.service;

import com.fs.srs.domain.User;
import com.fs.srs.exceptions.AuthenticationException;
import com.fs.srs.repository.UserRepository;

/**
 * this is our login authentication a user can only access if the login data is right
 */
public class AuthService {

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

   
    public User login(String username, String password) {
        if (username == null || password == null) {
            throw new AuthenticationException("Username and password are required"); /* when a user clicks logib without typing the credentials*/
        }
        User user = users.findByUsername(username)                                    /*for wrong login data*/
                .orElseThrow(() -> new AuthenticationException("Unknown user"));
        if (!user.getPassword().equals(password)) { 
            throw new AuthenticationException("Incorrect password");
        }
        return user;
    }
}
