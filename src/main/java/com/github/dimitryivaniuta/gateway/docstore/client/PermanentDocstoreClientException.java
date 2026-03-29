package com.github.dimitryivaniuta.gateway.docstore.client;

/**
 * Signals a non-transient Docstore client failure, such as a malformed request or a permanent
 * contract violation.
 */
public class PermanentDocstoreClientException extends DocstoreClientException {

    /**
     * Creates the exception.
     *
     * @param message human-readable error message
     */
    public PermanentDocstoreClientException(String message) {
        super(message);
    }

    /**
     * Creates the exception.
     *
     * @param message human-readable error message
     * @param cause root cause
     */
    public PermanentDocstoreClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
