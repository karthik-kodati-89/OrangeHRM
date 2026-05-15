package com.orangehrm.exceptions;

/**
 * Custom unchecked exception for framework-specific failures.
 * Demonstrates EXCEPTION HANDLING via custom exception hierarchy.
 */
public class FrameworkException extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
