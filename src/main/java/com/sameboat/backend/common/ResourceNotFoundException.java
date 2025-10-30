package com.sameboat.backend.common;

/**
 * Lightweight runtime exception to signal that a requested domain resource
 * does not exist. Mapped to HTTP 404 with error code NOT_FOUND by
 * {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}

