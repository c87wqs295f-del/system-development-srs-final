package com.fs.srs.service;

import com.fs.srs.domain.Employee;
import com.fs.srs.domain.User;
import com.fs.srs.exceptions.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private InMemoryUserRepository users;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        users.save(new Employee(null, "erik", "secret", "Erik", "e@x"));
        auth = new AuthService(users);
    }

    @Test
    void loginWithCorrectCredentialsReturnsUser() {
        User u = auth.login("erik", "secret");
        assertEquals("erik", u.getUsername());
        assertEquals("EMP", u.getRole());
    }

    @Test
    void loginWithWrongPasswordThrows() {
        assertThrows(AuthenticationException.class, () -> auth.login("erik", "nope"));
    }

    @Test
    void loginWithUnknownUsernameThrows() {
        assertThrows(AuthenticationException.class, () -> auth.login("ghost", "secret"));
    }

    @Test
    void loginWithNullsThrows() {
        assertThrows(AuthenticationException.class, () -> auth.login(null, null));
        assertThrows(AuthenticationException.class, () -> auth.login("erik", null));
        assertThrows(AuthenticationException.class, () -> auth.login(null, "secret"));
    }
}
