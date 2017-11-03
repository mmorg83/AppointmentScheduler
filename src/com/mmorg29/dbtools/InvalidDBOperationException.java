package com.mmorg29.dbtools;

/**
 *
 * @author mam
 * Exception for invalid operations of an object implementing the DBObject interface
 */
public class InvalidDBOperationException extends Exception {
    public InvalidDBOperationException() {
        super();
    }
    
    public InvalidDBOperationException(String message) {
        super(message);
    }
}
