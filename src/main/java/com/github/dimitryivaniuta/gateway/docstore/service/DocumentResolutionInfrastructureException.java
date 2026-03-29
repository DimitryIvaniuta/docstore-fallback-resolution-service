package com.github.dimitryivaniuta.gateway.docstore.service;

/**
 * Wraps terminal infrastructure failures raised while resolving a document.
 */
public class DocumentResolutionInfrastructureException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message human-readable error message
     * @param cause infrastructure cause
     */
    public DocumentResolutionInfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
