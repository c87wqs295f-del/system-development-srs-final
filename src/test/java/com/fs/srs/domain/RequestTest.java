package com.fs.srs.domain;

import com.fs.srs.exceptions.InvalidStatusTransitionException;
import com.fs.srs.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Proves the lifecycle rules encoded in {@link Request#transitionTo}. */
class RequestTest {

    private Employee submitter;
    private ServiceAgent agent;

    @BeforeEach
    void setUp() {
        submitter = new Employee(1L, "erik", "pw", "Erik", "e@x");
        agent     = new ServiceAgent(2L, "adam", "pw", "Adam", "a@x");
    }

    private Request freshRequest() {
        return new Request("title", "desc", Category.IT, Priority.MEDIUM, submitter);
    }

    @Test
    void newRequestStartsInNewStatus() {
        assertEquals(Status.NEW, freshRequest().getStatus());
    }

    @Test
    void newBlankTitleIsRejected() {
        assertThrows(ValidationException.class,
                () -> new Request("", "desc", Category.IT, Priority.LOW, submitter));
    }

    @Test
    void assignMovesNewToAssigned() {
        Request r = freshRequest();
        r.assign(agent);
        assertEquals(Status.ASSIGNED, r.getStatus());
        assertSame(agent, r.getAssignee());
    }

    @Test
    void happyPathLifecycle() {
        Request r = freshRequest();
        r.assign(agent);                          // NEW -> ASSIGNED
        r.transitionTo(Status.IN_PROGRESS);       // ASSIGNED -> IN_PROGRESS
        r.transitionTo(Status.WAITING_FOR_INFO);  // IN_PROGRESS -> WAITING_FOR_INFO
        r.transitionTo(Status.IN_PROGRESS);       // WAITING_FOR_INFO -> IN_PROGRESS
        r.transitionTo(Status.RESOLVED);          // IN_PROGRESS -> RESOLVED
        r.transitionTo(Status.CLOSED);            // RESOLVED -> CLOSED
        assertEquals(Status.CLOSED, r.getStatus());
        assertNotNull(r.getResolvedAt());
        assertNotNull(r.getClosedAt());
    }

    @Test
    void closedIsTerminal() {
        Request r = freshRequest();
        r.assign(agent);
        r.transitionTo(Status.IN_PROGRESS);
        r.transitionTo(Status.RESOLVED);
        r.transitionTo(Status.CLOSED);
        assertThrows(InvalidStatusTransitionException.class,
                () -> r.transitionTo(Status.IN_PROGRESS));
    }

    @Test
    void cannotSkipFromNewToInProgress() {
        Request r = freshRequest();
        assertThrows(InvalidStatusTransitionException.class,
                () -> r.transitionTo(Status.IN_PROGRESS));
    }

    @Test
    void cannotAssignAlreadyResolvedRequest() {
        Request r = freshRequest();
        r.assign(agent);
        r.transitionTo(Status.IN_PROGRESS);
        r.transitionTo(Status.RESOLVED);
        assertThrows(InvalidStatusTransitionException.class, () -> r.assign(agent));
    }

    @Test
    void resolvedCanBeReopened() {
        Request r = freshRequest();
        r.assign(agent);
        r.transitionTo(Status.IN_PROGRESS);
        r.transitionTo(Status.RESOLVED);
        r.transitionTo(Status.IN_PROGRESS);   // reopen
        assertEquals(Status.IN_PROGRESS, r.getStatus());
    }

    @Test
    void overdueFlagsMatchSla() {
        Request r = freshRequest();
        // Priority.MEDIUM -> 72h SLA; a fresh request is not overdue.
        assertFalse(r.isOverdue());
    }

    @Test
    void slaDeadlineUsesPriorityHours() {
        Request r = freshRequest();
        assertEquals(r.getCreatedAt().plusHours(72), r.getSlaDeadline());
    }
}
