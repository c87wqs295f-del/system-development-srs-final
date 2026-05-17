package com.fs.srs.repository;

import com.fs.srs.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over user storage. The service layer depends only on this
 * interface so that we could swap the
 * SQL implementation for an in memory one or a different database
 * without changimg any business code.
 */
public interface UserRepository {

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    void save(User user);
}
