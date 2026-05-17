package com.fs.srs.domain;

/** A regular employee EMP this category of user can submit requests and comment only on own requests. */
public class Employee extends User {

    public Employee(Long id, String username, String password, String fullName, String email) {
        super(id, username, password, fullName, email);
    }

    @Override
    public String getRole() {
        return "EMP";
    }
}
