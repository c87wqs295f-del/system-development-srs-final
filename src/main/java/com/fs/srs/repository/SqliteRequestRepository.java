package com.fs.srs.repository;

import com.fs.srs.domain.Category;
import com.fs.srs.domain.Comment;
import com.fs.srs.domain.Employee;
import com.fs.srs.domain.Priority;
import com.fs.srs.domain.Request;
import com.fs.srs.domain.ServiceAgent;
import com.fs.srs.domain.Status;
import com.fs.srs.domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** SQLite-backed implementation of {@link RequestRepository}. */
public class SqliteRequestRepository implements RequestRepository {

    private final Connection conn;
    private final UserRepository users;

    public SqliteRequestRepository(Database db, UserRepository users) {
        this.conn = db.getConnection();
        this.users = users;
    }

    @Override
    public Optional<Request> findById(long id) {
        return querySingle("SELECT * FROM requests WHERE id = ?", ps -> ps.setLong(1, id));
    }

    @Override
    public List<Request> findAll() {
        return queryMany("SELECT * FROM requests ORDER BY created_at DESC", ps -> { });
    }

    @Override
    public List<Request> findBySubmitter(long submitterId) {
        return queryMany("SELECT * FROM requests WHERE submitter_id = ? ORDER BY created_at DESC",
                ps -> ps.setLong(1, submitterId));
    }

    @Override
    public List<Request> findByAssignee(long assigneeId) {
        return queryMany("SELECT * FROM requests WHERE assignee_id = ? ORDER BY created_at DESC",
                ps -> ps.setLong(1, assigneeId));
    }

    @Override
    public List<Request> findByStatus(Status status) {
        return queryMany("SELECT * FROM requests WHERE status = ? ORDER BY created_at DESC",
                ps -> ps.setString(1, status.name()));
    }

    @Override
    public void save(Request request) {
        if (request.getId() == null) {
            insert(request);
        } else {
            update(request);
        }
        persistComments(request);
    }

    /* ------------------ INSERT / UPDATE ------------------ */

    private void insert(Request r) {
        String sql = """
                INSERT INTO requests
                  (title, description, category, priority, status,
                   submitter_id, assignee_id,
                   created_at, updated_at, resolved_at, closed_at)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindCommon(ps, r);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("insert(request) failed", e);
        }
    }

    private void update(Request r) {
        String sql = """
                UPDATE requests SET
                   title=?, description=?, category=?, priority=?, status=?,
                   submitter_id=?, assignee_id=?,
                   created_at=?, updated_at=?, resolved_at=?, closed_at=?
                WHERE id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindCommon(ps, r);
            ps.setLong(12, r.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("update(request) failed", e);
        }
    }

    private void bindCommon(PreparedStatement ps, Request r) throws SQLException {
        ps.setString(1, r.getTitle());
        ps.setString(2, r.getDescription());
        ps.setString(3, r.getCategory().name());
        ps.setString(4, r.getPriority().name());
        ps.setString(5, r.getStatus().name());
        ps.setLong(6,   r.getSubmitter().getId());
        if (r.getAssignee() == null) ps.setNull(7, java.sql.Types.INTEGER);
        else                         ps.setLong(7, r.getAssignee().getId());
        ps.setString(8,  r.getCreatedAt().toString());
        ps.setString(9,  r.getUpdatedAt().toString());
        ps.setString(10, r.getResolvedAt() == null ? null : r.getResolvedAt().toString());
        ps.setString(11, r.getClosedAt()   == null ? null : r.getClosedAt().toString());
    }

    /* ------------------ COMMENTS ------------------ */

    private void persistComments(Request r) {
        // Only brand-new comments need persisting. The simple strategy is:
        // a Comment with a null id has never been stored, so insert it.
        for (Comment c : r.getComments()) {
            if (c.getId() == null) insertComment(c);
        }
    }

    private void insertComment(Comment c) {
        String sql = "INSERT INTO comments (request_id, author_id, text, created_at) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, c.getRequestId());
            ps.setLong(2, c.getAuthor().getId());
            ps.setString(3, c.getText());
            ps.setString(4, c.getCreatedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("insert(comment) failed", e);
        }
    }

    /* ------------------ query helpers ------------------ */

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Optional<Request> querySingle(String sql, Binder binder) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Request r = rowToRequest(rs);
                    loadComments(r);
                    return Optional.of(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("query failed: " + sql, e);
        }
        return Optional.empty();
    }

    private List<Request> queryMany(String sql, Binder binder) {
        List<Request> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rowToRequest(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("query failed: " + sql, e);
        }
        // Comments loaded on demand on single finds; list views don't need them.
        return out;
    }

    private Request rowToRequest(ResultSet rs) throws SQLException {
        long id            = rs.getLong("id");
        String title       = rs.getString("title");
        String description = rs.getString("description");
        Category cat       = Category.valueOf(rs.getString("category"));
        Priority pri       = Priority.valueOf(rs.getString("priority"));
        Status st          = Status.valueOf(rs.getString("status"));
        long submitterId   = rs.getLong("submitter_id");

        User submitter = users.findById(submitterId)
                .orElseThrow(() -> new IllegalStateException("Missing submitter id=" + submitterId));
        if (!(submitter instanceof Employee emp)) {
            throw new IllegalStateException("Submitter is not an Employee: " + submitter);
        }

        ServiceAgent assignee = null;
        long assigneeId = rs.getLong("assignee_id");
        if (!rs.wasNull()) {
            User u = users.findById(assigneeId)
                    .orElseThrow(() -> new IllegalStateException("Missing assignee id=" + assigneeId));
            if (!(u instanceof ServiceAgent agent)) {
                throw new IllegalStateException("Assignee is not a ServiceAgent: " + u);
            }
            assignee = agent;
        }

        LocalDateTime created  = LocalDateTime.parse(rs.getString("created_at"));
        LocalDateTime updated  = LocalDateTime.parse(rs.getString("updated_at"));
        LocalDateTime resolved = rs.getString("resolved_at") == null ? null
                : LocalDateTime.parse(rs.getString("resolved_at"));
        LocalDateTime closed   = rs.getString("closed_at") == null ? null
                : LocalDateTime.parse(rs.getString("closed_at"));

        return new Request(id, title, description, cat, pri, st,
                emp, assignee, created, updated, resolved, closed);
    }

    private void loadComments(Request r) {
        String sql = "SELECT * FROM comments WHERE request_id = ? ORDER BY created_at";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long cid = rs.getLong("id");
                    long authorId = rs.getLong("author_id");
                    User author = users.findById(authorId)
                            .orElseThrow(() -> new IllegalStateException("Missing comment author id=" + authorId));
                    Comment c = new Comment(cid, r.getId(), author,
                            rs.getString("text"),
                            LocalDateTime.parse(rs.getString("created_at")));
                    r.loadComment(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("loadComments failed", e);
        }
    }
}
