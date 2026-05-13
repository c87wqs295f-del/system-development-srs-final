package com.fs.srs.domain;

/** A regular employee. Can submit requests and comment on their own requests. */
public class Employee extends User {

    public Employee(Long id, String username, String password, String fullName, String email) {
        super(id, username, password, fullName, email);
    }

    @Override
    public String getRole() {
        return "EMP";
    }
}
