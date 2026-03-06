package com.klu.backend.exception;

/**
 * Thrown when graph operations fail validation
 * (duplicate nodes, missing nodes, invalid probability, etc.).
 */
public class GraphValidationException extends RuntimeException {

    public GraphValidationException(String message) {
        super(message);
    }
}
