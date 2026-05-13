package com.fs.srs.repository;

import com.fs.srs.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over user storage. The service layer depends only on this
 * interface (Dependency Inversion, Session 6) so that we could swap the
 * SQLite implementation for an in-memory one or a different database
 * without touching any business code.
 */
public interface UserRepository {

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    /** Persists a new user and assigns an id, or updates an existing one. */
    void save(User user);
}
