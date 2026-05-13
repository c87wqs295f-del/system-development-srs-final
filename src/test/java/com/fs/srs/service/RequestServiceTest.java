package com.fs.srs.service;

import com.fs.srs.domain.Category;
import com.fs.srs.domain.Employee;
import com.fs.srs.domain.Manager;
import com.fs.srs.domain.Priority;
import com.fs.srs.domain.Request;
import com.fs.srs.domain.ServiceAgent;
import com.fs.srs.domain.Status;
import com.fs.srs.exceptions.AuthorizationException;
import com.fs.srs.exceptions.InvalidStatusTransitionException;
import com.fs.srs.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves the role-based authorization rules in {@link RequestService}.
 * <p>
 * Each test pairs a role (Employee / ServiceAgent / Manager) with an action
 * (create / assign / transition / comment / change priority) and asserts
 * success or {@link AuthorizationException}. Together these tests are the
 * executable specification of the permission matrix in the OOAD document.
 */
class RequestServiceTest {

    private InMemoryUserRepository users;
    private InMemoryRequestRepository requests;
    private RequestService service;

    private Employee emp;
    private Employee otherEmp;
    private ServiceAgent agent;
    private ServiceAgent otherAgent;
    private Manager manager;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        requests = new InMemoryRequestRepository();
        service = new RequestService(requests, users);

        emp        = new Employee(null, "erik", "pw", "Erik", "e@x");
        otherEmp   = new Employee(null, "elena", "pw", "Elena", "el@x");
        agent      = new ServiceAgent(null, "adam", "pw", "Adam", "a@x");
        otherAgent = new ServiceAgent(null, "ava", "pw", "Ava", "av@x");
        manager    = new Manager(null, "boss", "pw", "Boss", "b@x");
        for (var u : List.of(emp, otherEmp, agent, otherAgent, manager)) users.save(u);
    }

    private Request submitForEmp() {
        return service.createRequest(emp, "title", "desc", Category.IT, Priority.MEDIUM);
    }

    /* ================= createRequest ================= */

    @Test
    void employeeCanSubmitRequest() {
        Request r = submitForEmp();
        assertNotNull(r.getId());
        assertEquals(Status.NEW, r.getStatus());
        assertSame(emp, r.getSubmitter());
    }

    @Test
    void agentCannotSubmitRequest() {
        assertThrows(AuthorizationException.class,
                () -> service.createRequest(agent, "t", "d", Category.IT, Priority.LOW));
    }

    @Test
    void managerCannotSubmitRequest() {
        assertThrows(AuthorizationException.class,
                () -> service.createRequest(manager, "t", "d", Category.IT, Priority.LOW));
    }

    @Test
    void createWithMissingCategoryFails() {
        assertThrows(ValidationException.class,
                () -> service.createRequest(emp, "t", "d", null, Priority.LOW));
    }

    /* ================= assignRequest ================= */

    @Test
    void managerCanAssign() {
        Request r = submitForEmp();
        service.assignRequest(manager, r.getId(), agent.getId());
        assertEquals(Status.ASSIGNED, r.getStatus());
        assertEquals(agent.getId(), r.getAssignee().getId());
    }

    @Test
    void agentCanSelfAssign() {
        Request r = submitForEmp();
        service.assignRequest(agent, r.getId(), agent.getId());
        assertEquals(agent.getId(), r.getAssignee().getId());
    }

    @Test
    void agentCannotAssignAnotherAgent() {
        Request r = submitForEmp();
        assertThrows(AuthorizationException.class,
                () -> service.assignRequest(agent, r.getId(), otherAgent.getId()));
    }

    @Test
    void employeeCannotAssign() {
        Request r = submitForEmp();
        assertThrows(AuthorizationException.class,
                () -> service.assignRequest(emp, r.getId(), agent.getId()));
    }

    @Test
    void cannotAssignToNonAgentUser() {
        Request r = submitForEmp();
        assertThrows(ValidationException.class,
                () -> service.assignRequest(manager, r.getId(), emp.getId()));
    }

    /* ================= transitionStatus ================= */

    @Test
    void assigneeCanStartProgress() {
        Request r = submitForEmp();
        service.assignRequest(manager, r.getId(), agent.getId());
        service.transitionStatus(agent, r.getId(), Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, r.getStatus());
    }

    @Test
    void strangerAgentCannotTransition() {
        Request r = submitForEmp();
        service.assignRequest(manager, r.getId(), agent.getId());
        // otherAgent is not the assignee and not a manager
        assertThrows(AuthorizationException.class,
                () -> service.transitionStatus(otherAgent, r.getId(), Status.IN_PROGRESS));
    }

    @Test
    void assigneeCanResolve() {
        Request r = submitForEmp();
        service.assignRequest(manager, r.getId(), agent.getId());
        service.transitionStatus(agent, r.getId(), Status.IN_PROGRESS);
        service.transitionStatus(agent, r.getId(), Status.RESOLVED);
        assertEquals(Status.RESOLVED, r.getStatus());
    }

    @Test
    void submitterCanClose() {
        Request r = driveToResolved();
        service.transitionStatus(emp, r.getId(), Status.CLOSED);
        assertEquals(Status.CLOSED, r.getStatus());
    }

    @Test
    void nonSubmitterEmployeeCannotClose() {
        Request r = driveToResolved();
        assertThrows(AuthorizationException.class,
                () -> service.transitionStatus(otherEmp, r.getId(), Status.CLOSED));
    }

    @Test
    void invalidStatusTransitionIsRejected() {
        Request r = submitForEmp();
        // NEW -> IN_PROGRESS is illegal (must go via ASSIGNED)
        assertThrows(InvalidStatusTransitionException.class,
                () -> service.transitionStatus(manager, r.getId(), Status.IN_PROGRESS));
    }

    /* ================= addComment ================= */

    @Test
    void submitterCanCommentOnOwnRequest() {
        Request r = submitForEmp();
        service.addComment(emp, r.getId(), "hello");
        assertEquals(1, r.getComments().size());
    }

    @Test
    void assigneeCanComment() {
        Request r = submitForEmp();
        service.assignRequest(manager, r.getId(), agent.getId());
        service.addComment(agent, r.getId(), "on it");
        assertEquals(1, r.getComments().size());
    }

    @Test
    void managerCanCommentOnAnything() {
        Request r = submitForEmp();
        service.addComment(manager, r.getId(), "noted");
        assertEquals(1, r.getComments().size());
    }

    @Test
    void strangerEmployeeCannotComment() {
        Request r = submitForEmp();
        assertThrows(AuthorizationException.class,
                () -> service.addComment(otherEmp, r.getId(), "nope"));
    }

    @Test
    void emptyCommentRejected() {
        Request r = submitForEmp();
        assertThrows(ValidationException.class, () -> service.addComment(emp, r.getId(), "  "));
    }

    /* ================= changePriority ================= */

    @Test
    void managerCanChangePriority() {
        Request r = submitForEmp();
        service.changePriority(manager, r.getId(), Priority.URGENT);
        assertEquals(Priority.URGENT, r.getPriority());
    }

    @Test
    void agentCannotChangePriority() {
        Request r = submitForEmp();
        assertThrows(AuthorizationException.class,
                () -> service.changePriority(agent, r.getId(), Priority.URGENT));
    }

    @Test
    void employeeCannotChangePriority() {
        Request r = submitForEmp();
        assertThrows(AuthorizationException.class,
                () -> service.changePriority(emp, r.getId(), Priority.URGENT));
    }

    /* ================= visibility / listFor ================= */

    @Test
    void managerSeesAllRequests() {
        submitForEmp();                                                           // emp
        service.createRequest(otherEmp, "t2","d",Category.IT,Priority.LOW);       // otherEmp
        assertEquals(2, service.listFor(manager).size());
    }

    @Test
    void employeeSeesOnlyOwnRequests() {
        submitForEmp();                                                           // visible
        service.createRequest(otherEmp, "t2","d",Category.IT,Priority.LOW);       // hidden
        List<Request> mine = service.listFor(emp);
        assertEquals(1, mine.size());
        assertEquals(emp.getId(), mine.get(0).getSubmitter().getId());
    }

    @Test
    void agentSeesOnlyAssignedRequests() {
        Request r1 = submitForEmp();
        service.assignRequest(manager, r1.getId(), agent.getId());
        service.createRequest(otherEmp, "t2","d",Category.IT,Priority.LOW);       // unassigned
        List<Request> assigned = service.listFor(agent);
        assertEquals(1, assigned.size());
        assertEquals(r1.getId(), assigned.get(0).getId());
    }

    @Test
    void employeeCannotViewAnotherEmployeesRequest() {
        Request r = submitForEmp();
        assertThrows(AuthorizationException.class,
                () -> service.getVisible(otherEmp, r.getId()));
    }

    /* ================= helpers ================= */

    private Request driveToResolved() {
        Request r = submitForEmp();
        service.assignRequest(manager, r.getId(), agent.getId());
        service.transitionStatus(agent, r.getId(), Status.IN_PROGRESS);
        service.transitionStatus(agent, r.getId(), Status.RESOLVED);
        return r;
    }
}
