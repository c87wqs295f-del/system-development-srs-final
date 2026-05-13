package com.fs.srs.service;

import com.fs.srs.domain.Request;
import com.fs.srs.domain.Status;
import com.fs.srs.repository.RequestRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory {@link RequestRepository} used by service tests. */
class InMemoryRequestRepository implements RequestRepository {

    private final Map<Long, Request> byId = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Optional<Request> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Request> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public List<Request> findBySubmitter(long submitterId) {
        return byId.values().stream()
                .filter(r -> r.getSubmitter().getId() == submitterId)
                .toList();
    }

    @Override
    public List<Request> findByAssignee(long assigneeId) {
        return byId.values().stream()
                .filter(r -> r.getAssignee() != null && r.getAssignee().getId() == assigneeId)
                .toList();
    }

    @Override
    public List<Request> findByStatus(Status status) {
        return byId.values().stream()
                .filter(r -> r.getStatus() == status)
                .toList();
    }

    @Override
    public void save(Request request) {
        if (request.getId() == null) request.setId(nextId.getAndIncrement());
        byId.put(request.getId(), request);
    }
}
