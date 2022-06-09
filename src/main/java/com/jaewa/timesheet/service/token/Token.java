package com.jaewa.timesheet.service.token;


import java.security.Principal;

public class Token implements Principal {

    private final String subject;
    public Token(String subject) {
        this.subject = subject;
    }

    @Override
    public String getName() {
        return this.subject;
    }
}

