package com.fs.srs.domain;

import com.fs.srs.exceptions.InvalidStatusTransitionException;
import com.fs.srs.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A service request submitted by an Employee and handled by a
 * ServiceAgent.
 * <p>
 * Owns its own lifecycle. The transition rules between Status values
 * are enforced in  #transitionTo(Status) callers cannot bypass them.
 */
public class Request {

    /**
     * Allowed status transitions. If the FROM state is missing from this map,
     * the state is terminal. If the TO state is not in the set, the transition
     * is illegal.
     */
    private static final Map<Status, Set<Status>> ALLOWED_TRANSITIONS = Map.of(
            Status.NEW,              Set.of(Status.ASSIGNED),
            Status.ASSIGNED,         Set.of(Status.IN_PROGRESS, Status.NEW),
            Status.IN_PROGRESS,      Set.of(Status.WAITING_FOR_INFO, Status.RESOLVED),
            Status.WAITING_FOR_INFO, Set.of(Status.IN_PROGRESS),
            Status.RESOLVED,         Set.of(Status.CLOSED, Status.IN_PROGRESS),
            Status.CLOSED,           Set.of()
    );

    private Long id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private Status status;
    private final Employee submitter;
    private ServiceAgent assignee;               // null until assigned
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;            // null until RESOLVED
    private LocalDateTime closedAt;              // null until CLOSED
    private final List<Comment> comments = new ArrayList<>();

    /** Full constructor used by the repository when rebuilding a row. */
    public Request(Long id, String title, String description,
                   Category category, Priority priority, Status status,
                   Employee submitter, ServiceAgent assignee,
                   LocalDateTime createdAt, LocalDateTime updatedAt,
                   LocalDateTime resolvedAt, LocalDateTime closedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.submitter = submitter;
        this.assignee = assignee;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
        this.closedAt = closedAt;
    }

    /** Convenience constructor for a brand-new request. */
    public Request(String title, String description, Category category, Priority priority, Employee submitter) {
        this(null, title, description, category, priority, Status.NEW,
                submitter, null, LocalDateTime.now(), LocalDateTime.now(), null, null);
        validateTitle(title);
    }

    /* ------------------------------------------------------------------ */
    /*  Domain behavior (the methods that appear on the UML diagram)       */
    /* ------------------------------------------------------------------ */

    /**
     * Move this request into {@code newStatus} if the transition is allowed,
     * otherwise throw. Also records the timestamp of RESOLVED / CLOSED.
     */
    public void transitionTo(Status newStatus) {
        Set<Status> allowed = ALLOWED_TRANSITIONS.getOrDefault(this.status, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot move request from " + this.status + " to " + newStatus);
        }
        this.status = newStatus;
        LocalDateTime now = LocalDateTime.now();
        this.updatedAt = now;
        if (newStatus == Status.RESOLVED) this.resolvedAt = now;
        if (newStatus == Status.CLOSED)   this.closedAt = now;
    }

    /**
     * Assign this request to an agent. Legal only when the request is NEW or
     * already ASSIGNED (reassignment). Moves NEW -> ASSIGNED automatically.
     */
    public void assign(ServiceAgent agent) {
        if (agent == null) {
            throw new ValidationException("Assignee must not be null");
        }
        if (this.status != Status.NEW && this.status != Status.ASSIGNED) {
            throw new InvalidStatusTransitionException(
                    "Cannot assign a request that is already " + this.status);
        }
        this.assignee = agent;
        if (this.status == Status.NEW) {
            transitionTo(Status.ASSIGNED);
        } else {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /** Add a comment to this request. */
    public void addComment(Comment comment) {
        if (comment == null) throw new ValidationException("Comment must not be null");
        this.comments.add(comment);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Used by the repository to rehydrate comments from storage without
     * touching  updatedAt (the comment was already saved, so the
     * request has not actually been modified now).
     */
    public void loadComment(Comment comment) {
        if (comment == null) throw new ValidationException("Comment must not be null");
        this.comments.add(comment);
    }

    /**
     *  if the current time is past the Service Level Agreenent deadline and
     *  the request is not yet RESOLVED or CLOSED.
     */
    public boolean isOverdue() {
        if (status == Status.RESOLVED || status == Status.CLOSED) return false;
        return LocalDateTime.now().isAfter(getSlaDeadline());
    }

    /** @return the moment by which this request must be resolved to meet its SLA. */
    public LocalDateTime getSlaDeadline() {
        return createdAt.plusHours(priority.getSlaHours());
    }

    
    /*  Getters / setters  */


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { validateTitle(title); this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; this.updatedAt = LocalDateTime.now(); }
    public Status getStatus() { return status; }
    public Employee getSubmitter() { return submitter; }
    public ServiceAgent getAssignee() { return assignee; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public List<Comment> getComments() { return Collections.unmodifiableList(comments); }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Request title must not be empty");
        }
        if (title.length() > 120) {
            throw new ValidationException("Request title must be 120 characters or fewer");
        }
    }
}
