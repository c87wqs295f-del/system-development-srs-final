package com.fs.srs.domain;

/** A manager can reassign, change priority, see all requests, and generate reports. */
public class Manager extends User {

    public Manager(Long id, String username, String password, String fullName, String email) {
        super(id, username, password, fullName, email);
    }

    @Override
    public String getRole() {
        return "MGR";
    }
}
