package com.mmorg29.agenda;

/**
 *
 * @author mam
 * This is a basic Exception class that is thrown when an issue occurs during an operation involving a Monthly, Weekly, or Daily Agenda object
 */
public class InvalidAgendaException extends Exception {
    
    InvalidAgendaException() {
        super();
    }

    InvalidAgendaException(String message) {
        super(message);
    }
    
}
