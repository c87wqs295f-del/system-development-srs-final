package com.fs.srs.service;

import com.fs.srs.domain.User;
import com.fs.srs.repository.UserRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory {@link UserRepository} used by service tests.
 * <p>
 * This is the concrete pay-off of the Dependency Inversion Principle
 * (Session 6): the services don't care whether their repository is SQLite
 * or a {@code HashMap}, so tests pick the faster option and run in milliseconds.
 */
class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> byId = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return byId.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public void save(User user) {
        if (user.getId() == null) user.setId(nextId.getAndIncrement());
        byId.put(user.getId(), user);
    }
}
