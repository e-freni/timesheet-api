package com.jaewa.timesheet.exception;

public class MailSendingException extends Exception {
    public MailSendingException(String errorMessage) {
        super(errorMessage);
    }
}
