package com.github.dimitryivaniuta.gateway.docstore.client;

/**
 * Base runtime exception for infrastructure failures raised by a {@link DocstoreClient}
 * implementation.
 */
public class DocstoreClientException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message human-readable error message
     */
    public DocstoreClientException(String message) {
        super(message);
    }

    /**
     * Creates the exception.
     *
     * @param message human-readable error message
     * @param cause root cause
     */
    public DocstoreClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
