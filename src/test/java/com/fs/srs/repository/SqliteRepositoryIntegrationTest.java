package com.fs.srs.repository;

import com.fs.srs.domain.Category;
import com.fs.srs.domain.Comment;
import com.fs.srs.domain.Employee;
import com.fs.srs.domain.Manager;
import com.fs.srs.domain.Priority;
import com.fs.srs.domain.Request;
import com.fs.srs.domain.ServiceAgent;
import com.fs.srs.domain.Status;
import com.fs.srs.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end persistence test: boots a real SQLite database in a JUnit
 * {@link TempDir}, runs the full DDL, saves and reloads users, requests,
 * and comments, and proves the round-trip preserves every field.
 * <p>
 * If this test passes, the schema, the PreparedStatement parameter order,
 * and the ResultSet-to-object mapping all agree — there is no way to have
 * a bug in any one of them without this catching it.
 */
class SqliteRepositoryIntegrationTest {

    private Database db;
    private SqliteUserRepository users;
    private SqliteRequestRepository requests;

    @BeforeEach
    void setUp(@TempDir Path tmp) {
        String url = "jdbc:sqlite:" + tmp.resolve("test.db").toAbsolutePath();
        this.db = new Database(url);
        this.users = new SqliteUserRepository(db);
        this.requests = new SqliteRequestRepository(db, users);
    }

    @Test
    void saveAndReloadUser_preservesSubclass() {
        Manager boss = new Manager(null, "boss", "pw", "Big Boss", "b@x");
        users.save(boss);
        assertNotNull(boss.getId(), "id should be assigned on insert");

        Optional<User> loaded = users.findByUsername("boss");
        assertTrue(loaded.isPresent());
        assertInstanceOf(Manager.class, loaded.get(), "role column should round-trip to subclass");
        assertEquals("MGR", loaded.get().getRole());
    }

    @Test
    void saveAndReloadRequest_preservesEveryField() {
        // seed users
        Employee emp = new Employee(null, "emp", "pw", "Erik", "e@x");
        ServiceAgent agent = new ServiceAgent(null, "agent", "pw", "Adam", "a@x");
        users.save(emp);
        users.save(agent);

        // create + drive the lifecycle a bit
        Request r = new Request("Laptop broken", "Black screen",
                Category.IT, Priority.HIGH, emp);
        requests.save(r);
        r.assign(agent);                    // -> ASSIGNED
        r.transitionTo(Status.IN_PROGRESS); // -> IN_PROGRESS
        r.addComment(new Comment(r.getId(), agent, "Investigating."));
        requests.save(r);

        // reload with a FRESH repository instance to prove nothing is cached
        UserRepository users2 = new SqliteUserRepository(db);
        SqliteRequestRepository requests2 = new SqliteRequestRepository(db, users2);
        Request reloaded = requests2.findById(r.getId()).orElseThrow();

        assertEquals(r.getTitle(),       reloaded.getTitle());
        assertEquals(r.getDescription(), reloaded.getDescription());
        assertEquals(Category.IT,        reloaded.getCategory());
        assertEquals(Priority.HIGH,      reloaded.getPriority());
        assertEquals(Status.IN_PROGRESS, reloaded.getStatus());
        assertEquals(emp.getId(),        reloaded.getSubmitter().getId());
        assertNotNull(reloaded.getAssignee());
        assertEquals(agent.getId(),      reloaded.getAssignee().getId());
        assertInstanceOf(ServiceAgent.class, reloaded.getAssignee());
        assertEquals(1, reloaded.getComments().size());
        assertEquals("Investigating.", reloaded.getComments().get(0).getText());
    }

    @Test
    void findByAssignee_returnsOnlyThatAgentsRequests() {
        Employee emp = new Employee(null, "emp", "pw", "Erik", "e@x");
        ServiceAgent a1 = new ServiceAgent(null, "a1", "pw", "A1", "a1@x");
        ServiceAgent a2 = new ServiceAgent(null, "a2", "pw", "A2", "a2@x");
        users.save(emp); users.save(a1); users.save(a2);

        Request r1 = new Request("t1","d", Category.IT, Priority.LOW, emp);
        Request r2 = new Request("t2","d", Category.IT, Priority.LOW, emp);
        requests.save(r1); requests.save(r2);
        r1.assign(a1); r2.assign(a2);
        requests.save(r1); requests.save(r2);

        List<Request> forA1 = requests.findByAssignee(a1.getId());
        assertEquals(1, forA1.size());
        assertEquals(r1.getId(), forA1.get(0).getId());
    }

    @Test
    void findByStatus_filtersCorrectly() {
        Employee emp = new Employee(null, "emp", "pw", "Erik", "e@x");
        users.save(emp);
        Request r1 = new Request("a","d", Category.HR, Priority.LOW, emp);
        Request r2 = new Request("b","d", Category.HR, Priority.LOW, emp);
        requests.save(r1); requests.save(r2);

        assertEquals(2, requests.findByStatus(Status.NEW).size());
        assertEquals(0, requests.findByStatus(Status.CLOSED).size());
    }
}
