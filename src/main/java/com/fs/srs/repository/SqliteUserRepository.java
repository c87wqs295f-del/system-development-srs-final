package com.fs.srs.repository;

import com.fs.srs.domain.Employee;
import com.fs.srs.domain.Manager;
import com.fs.srs.domain.ServiceAgent;
import com.fs.srs.domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQL backed implementation of UserRepository.
 */
public class SqliteUserRepository implements UserRepository {

    private final Connection conn;

    public SqliteUserRepository(Database db) {
        this.conn = db.getConnection();
    }

    @Override
    public Optional<User> findById(long id) {
        return querySingle("SELECT * FROM users WHERE id = ?", ps -> ps.setLong(1, id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return querySingle("SELECT * FROM users WHERE username = ?", ps -> ps.setString(1, username));
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(rowToUser(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll(users) failed", e);
        }
        return users;
    }

    @Override
    public void save(User user) {
        if (user.getId() == null) {
            insert(user);
        } else {
            update(user);
        }
    }

    private void insert(User user) {
        String sql = "INSERT INTO users (username, password, full_name, email, role) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("insert(user) failed", e);
        }
    }

    private void update(User user) {
        String sql = "UPDATE users SET username=?, password=?, full_name=?, email=?, role=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole());
            ps.setLong(6, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("update(user) failed", e);
        }
    }

    /* helpers */

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Optional<User> querySingle(String sql, Binder binder) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rowToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("query failed: " + sql, e);
        }
        return Optional.empty();
    }

    /** picks the right subclass from the role column. */
    static User rowToUser(ResultSet rs) throws SQLException {
        long id         = rs.getLong("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String fullName = rs.getString("full_name");
        String email    = rs.getString("email");
        String role     = rs.getString("role");
        return switch (role) {
            case "EMP"   -> new Employee(id, username, password, fullName, email);
            case "AGENT" -> new ServiceAgent(id, username, password, fullName, email);
            case "MGR"   -> new Manager(id, username, password, fullName, email);
            default      -> throw new IllegalStateException("Unknown role in DB: " + role);
        };
    }
}
