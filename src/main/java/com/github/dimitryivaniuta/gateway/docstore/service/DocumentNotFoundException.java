package com.github.dimitryivaniuta.gateway.docstore.service;

/**
 * Thrown when a document cannot be resolved under any configured identifier.
 */
public class DocumentNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message human-readable error message
     */
    public DocumentNotFoundException(String message) {
        super(message);
    }
}
