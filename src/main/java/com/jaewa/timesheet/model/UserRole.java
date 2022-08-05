package com.jaewa.timesheet.model;

public enum UserRole {

    ADMINISTRATOR("ROLE_ADMIN"),
    USER("ROLE_USER");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
