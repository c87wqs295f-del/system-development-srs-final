package com.fs.srs.repository;

import com.fs.srs.domain.Request;
import com.fs.srs.domain.Status;

import java.util.List;
import java.util.Optional;

/** Abstraction over request storage. See UserRepository for rationale. */
public interface RequestRepository {

    Optional<Request> findById(long id);

    List<Request> findAll();

    List<Request> findBySubmitter(long submitterId);

    List<Request> findByAssignee(long assigneeId);

    List<Request> findByStatus(Status status);

    /** Saves a new request and assigns an id or updates an existing one. */
    void save(Request request);
}
