package com.ie207.vagabond.exception;

public class TicketNotInTourException extends RuntimeException {
    public TicketNotInTourException(String message) {
        super(message);
    }

    public TicketNotInTourException(String message, Throwable cause) {
        super(message, cause);
    }
}