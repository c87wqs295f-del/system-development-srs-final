package com.fs.srs.service;

import com.fs.srs.domain.Category;
import com.fs.srs.domain.Comment;
import com.fs.srs.domain.Employee;
import com.fs.srs.domain.Manager;
import com.fs.srs.domain.Priority;
import com.fs.srs.domain.Request;
import com.fs.srs.domain.ServiceAgent;
import com.fs.srs.domain.Status;
import com.fs.srs.domain.User;
import com.fs.srs.exceptions.AuthorizationException;
import com.fs.srs.exceptions.ValidationException;
import com.fs.srs.repository.RequestRepository;
import com.fs.srs.repository.UserRepository;

import java.util.List;

/**
 * Application service that orchestrates request operations and enforces
 * role-based authorization. It relies on {@link Request}'s own domain
 * methods for lifecycle invariants — this service does not re-implement them.
 * <p>
 * Depends on the repository interfaces only (Dependency Inversion). Swapping
 * SQLite for another backing store does not change this class.
 */
public class RequestService {

    private final RequestRepository requests;
    private final UserRepository users;

    public RequestService(RequestRepository requests, UserRepository users) {
        this.requests = requests;
        this.users = users;
    }

    /** Employees submit new requests. */
    public Request createRequest(User actor, String title, String description,
                                 Category category, Priority priority) {
        if (!(actor instanceof Employee employee)) {
            throw new AuthorizationException("Only employees can submit requests");
        }
        if (category == null) throw new ValidationException("Category is required");
        if (priority == null) throw new ValidationException("Priority is required");

        Request r = new Request(title, description, category, priority, employee);
        requests.save(r);
        return r;
    }

    /** Managers (or the target agent themselves) assign a request to an agent. */
    public Request assignRequest(User actor, long requestId, long agentId) {
        Request r = requireRequest(requestId);
        User agent = users.findById(agentId)
                .orElseThrow(() -> new ValidationException("Agent not found"));
        if (!(agent instanceof ServiceAgent sa)) {
            throw new ValidationException("User " + agent.getUsername() + " is not a service agent");
        }
        boolean isManager = actor instanceof Manager;
        boolean isSelfAssign = actor instanceof ServiceAgent && actor.getId().equals(agentId);
        if (!isManager && !isSelfAssign) {
            throw new AuthorizationException("Only a manager (or the agent themselves) can assign");
        }
        r.assign(sa);
        requests.save(r);
        return r;
    }

    /** Transition a request's status; authorization depends on the target state. */
    public Request transitionStatus(User actor, long requestId, Status newStatus) {
        Request r = requireRequest(requestId);
        authorizeTransition(actor, r, newStatus);
        r.transitionTo(newStatus);
        requests.save(r);
        return r;
    }

    /** Change priority (managers only). */
    public Request changePriority(User actor, long requestId, Priority priority) {
        if (!(actor instanceof Manager)) {
            throw new AuthorizationException("Only a manager can change priority");
        }
        Request r = requireRequest(requestId);
        r.setPriority(priority);
        requests.save(r);
        return r;
    }

    /** Add a comment. Authors: submitter, assignee, or any manager. */
    public Request addComment(User actor, long requestId, String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Comment text must not be empty");
        }
        Request r = requireRequest(requestId);
        boolean isSubmitter = r.getSubmitter().getId().equals(actor.getId());
        boolean isAssignee  = r.getAssignee() != null && r.getAssignee().getId().equals(actor.getId());
        boolean isManager   = actor instanceof Manager;
        if (!isSubmitter && !isAssignee && !isManager) {
            throw new AuthorizationException("You cannot comment on this request");
        }
        r.addComment(new Comment(r.getId(), actor, text));
        requests.save(r);
        return r;
    }

    /* ------------------ queries ------------------ */

    /**
     * Return the list of requests visible to {@code actor}:
     *   Employee  -> their submissions;
     *   Agent     -> their assignments;
     *   Manager   -> everything.
     */
    public List<Request> listFor(User actor) {
        if (actor instanceof Manager)        return requests.findAll();
        if (actor instanceof ServiceAgent)   return requests.findByAssignee(actor.getId());
        if (actor instanceof Employee)       return requests.findBySubmitter(actor.getId());
        throw new AuthorizationException("Unknown role: " + actor.getRole());
    }

    public Request getVisible(User actor, long requestId) {
        Request r = requireRequest(requestId);
        boolean visible =
                actor instanceof Manager
                || r.getSubmitter().getId().equals(actor.getId())
                || (r.getAssignee() != null && r.getAssignee().getId().equals(actor.getId()));
        if (!visible) {
            throw new AuthorizationException("You cannot view this request");
        }
        return r;
    }

  

    private Request requireRequest(long id) {
        return requests.findById(id)
                .orElseThrow(() -> new ValidationException("Request " + id + " not found"));
    }


    private void authorizeTransition(User actor, Request r, Status target) {
        boolean isManager  = actor instanceof Manager;
        boolean isAssignee = r.getAssignee() != null && r.getAssignee().getId().equals(actor.getId());
        boolean isSubmitter = r.getSubmitter().getId().equals(actor.getId());

        switch (target) {
            case ASSIGNED -> {
                if (!isManager && !(actor instanceof ServiceAgent)) {
                    throw new AuthorizationException("Only managers or agents can assign");
                }
            }
            case IN_PROGRESS -> {
                if (!isManager && !isAssignee && !isSubmitter) {
                    throw new AuthorizationException("Only the assignee, submitter, or a manager can move to IN_PROGRESS");
                }
            }
            case WAITING_FOR_INFO, RESOLVED -> {
                if (!isManager && !isAssignee) {
                    throw new AuthorizationException("Only the assignee or a manager can do this");
                }
            }
            case CLOSED -> {
                if (!isManager && !isSubmitter) {
                    throw new AuthorizationException("Only the submitter or a manager can close");
                }
            }
            case NEW -> {
                if (!isManager) {
                    throw new AuthorizationException("Only a manager can unassign");
                }
            }
        }
    }
}
