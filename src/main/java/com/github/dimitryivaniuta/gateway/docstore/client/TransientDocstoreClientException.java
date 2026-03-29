package com.github.dimitryivaniuta.gateway.docstore.client;

/**
 * Signals a transient infrastructure failure when talking to Docstore, such as a timeout, a brief
 * gateway problem, or a temporary dependency outage.
 */
public class TransientDocstoreClientException extends DocstoreClientException {

    /**
     * Creates the exception.
     *
     * @param message human-readable error message
     */
    public TransientDocstoreClientException(String message) {
        super(message);
    }

    /**
     * Creates the exception.
     *
     * @param message human-readable error message
     * @param cause root cause
     */
    public TransientDocstoreClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
