package com.github.dimitryivaniuta.gateway.docstore.client;

import com.github.dimitryivaniuta.gateway.docstore.model.StoredDocument;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Small retry decorator for a {@link DocstoreClient}.
 *
 * <p>This class is intentionally dependency-free so it can be replaced later by a framework-backed
 * retry implementation if desired. It only retries {@link TransientDocstoreClientException}. A
 * normal business miss still returns {@link Optional#empty()} without retrying.</p>
 */
public final class RetryingDocstoreClientDecorator implements DocstoreClient {

    private final DocstoreClient delegate;
    private final int maxAttempts;
    private final Duration backoff;

    /**
     * Creates the retrying decorator.
     *
     * @param delegate    wrapped Docstore client
     * @param maxAttempts maximum call attempts, including the first call
     * @param backoff     backoff between transient retries
     */
    public RetryingDocstoreClientDecorator(DocstoreClient delegate, int maxAttempts, Duration backoff) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.backoff = Objects.requireNonNull(backoff, "backoff must not be null");
        if (backoff.isNegative()) {
            throw new IllegalArgumentException("backoff must not be negative");
        }
    }

    /**
     * Executes the delegate call with transient retry behavior.
     *
     * @param identifier   canonical or legacy Docstore identifier
     * @param documentName logical document name
     * @return document payload or empty optional when not found
     */
    @Override
    public Optional<StoredDocument> fetch(String identifier, String documentName) {
        int attempt = 1;
        while (true) {
            try {
                return delegate.fetch(identifier, documentName);
            } catch (TransientDocstoreClientException ex) {
                if (attempt >= maxAttempts) {
                    throw ex;
                }
                sleepBackoff();
                attempt++;
            }
        }
    }

    private void sleepBackoff() {
        if (backoff.isZero()) {
            return;
        }
        try {
            Thread.sleep(backoff);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new TransientDocstoreClientException("Retry interrupted while waiting for Docstore backoff", interruptedException);
        }
    }
}
