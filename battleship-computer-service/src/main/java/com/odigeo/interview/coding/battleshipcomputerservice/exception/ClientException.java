package com.odigeo.interview.coding.battleshipcomputerservice.exception;

/**
 * Runtime Exception for client calls
 */
public class ClientException extends RuntimeException {

    public ClientException(String operation, int httpStatus, String message) {
        super(String.format("Error %d calling %s: %s", httpStatus, operation, message));
    }

}
