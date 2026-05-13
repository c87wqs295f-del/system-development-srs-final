package com.fs.srs.domain;

/**
 * Abstract base class for every person who can log into the system.
 * <p>
 * You never instantiate a generic {@code User} — you always have a concrete
 * {@link Employee}, {@link ServiceAgent} or {@link Manager}. This is exactly
 * the situation Session 5 names "a placeholder in the hierarchy", and so we
 * declare {@code User} {@code abstract} to make that intent explicit and
 * prevent {@code new User(...)} at compile time.
 * <p>
 * Shared attributes (id, credentials, contact info) live here so the three
 * subclasses do not duplicate them — this is inheritance used for a real
 * "is-a" relationship (Session 2), not just for code reuse.
 */
public abstract class User {

    private Long id;                // null until the repository assigns one
    private final String username;
    private final String password;  // plain text by deliberate Phase-1 scope decision
    private final String fullName;
    private final String email;

    protected User(Long id, String username, String password, String fullName, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }

    /**
     * Each concrete subclass returns the short role code used for authorization
     * checks. This is polymorphism in action: callers work with {@code User}
     * and never need to know which concrete class they have.
     *
     * @return role code, e.g. "EMP", "AGENT", "MGR"
     */
    public abstract String getRole();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return getRole() + "(" + username + ")";
    }
}
