package com.jaewa.timesheet.service.token;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenTest {

    @Test
    void getNameShouldReturnSubject() {
        String subject = "user123";
        Long applicationUserId = 1L;

        Token token = new Token(subject, applicationUserId);

        assertEquals(subject, token.getName());
    }

    @Test
    void getApplicationUserIdShouldReturnApplicationUserId() {
        String subject = "user123";
        Long applicationUserId = 1L;

        Token token = new Token(subject, applicationUserId);

        assertEquals(applicationUserId, token.getApplicationUserId());
    }
}
