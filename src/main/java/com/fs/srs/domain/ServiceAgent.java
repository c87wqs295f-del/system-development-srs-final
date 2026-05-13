package com.fs.srs.domain;

/** A service desk agent. Can be assigned requests and drive them through the lifecycle. */
public class ServiceAgent extends User {

    public ServiceAgent(Long id, String username, String password, String fullName, String email) {
        super(id, username, password, fullName, email);
    }

    @Override
    public String getRole() {
        return "AGENT";
    }
}
