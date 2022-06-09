package com.jaewa.timesheet.model;

public enum UserRole {

    ADMINSTRATOR("ROLE_SYSTEM_ADMIN"),
    USER("ROLE_USER");

    private String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
