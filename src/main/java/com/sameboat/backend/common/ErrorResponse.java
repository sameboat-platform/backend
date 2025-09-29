package com.sameboat.backend.common;

/**
 * Simple JSON error payload containing a machine-readable error code and a
 * human-friendly message suitable for display.
 *
 * @param error   stable error code (e.g. VALIDATION_ERROR)
 * @param message descriptive message
 */
public record ErrorResponse(String error, String message) { }
