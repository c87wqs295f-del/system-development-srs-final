package com.fs.srs.domain;

/**
 * That is the base class for the three user categories who can log in the system.
 */
public abstract class User {

    private Long id;                // null until the repository assigns one
    private final String username;
    private final String password;  // will be placeholder password so it is easier to tst
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
     * Each subclass returns the role "EMP", "AGENT", "MGR"
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
