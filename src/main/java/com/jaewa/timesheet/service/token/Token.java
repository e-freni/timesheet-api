package com.jaewa.timesheet.service.token;


import java.security.Principal;

public class Token implements Principal {

    private final String subject;
    private final Long applicationUserId;
    public Token(String subject, Long applicationUserId) {
        this.subject = subject;
        this.applicationUserId = applicationUserId;
    }

    @Override
    public String getName() {
        return this.subject;
    }

    public Long getApplicationUserId() {
        return this.applicationUserId;
    }
}

