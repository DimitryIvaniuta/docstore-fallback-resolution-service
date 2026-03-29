package com.github.dimitryivaniuta.gateway.docstore.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable outcome of a Docstore resolution attempt.
 *
 * <p>The result can represent a success or a business miss. Infrastructure failures are represented
 * as exceptions and are therefore not modelled as a normal result.</p>
 *
 * @param document resolved document for successful lookups
 * @param resolutionPath final path outcome
 * @param attempts ordered lookup attempts performed by the resolver
 * @param totalDurationNanos total time spent resolving the request
 */
public record DocumentResolutionResult(
        StoredDocument document,
        ResolutionPath resolutionPath,
        List<LookupAttempt> attempts,
        long totalDurationNanos) {

    /**
     * Creates and validates the resolution result.
     */
    public DocumentResolutionResult {
        Objects.requireNonNull(resolutionPath, "resolutionPath must not be null");
        Objects.requireNonNull(attempts, "attempts must not be null");
        if (totalDurationNanos < 0) {
            throw new IllegalArgumentException("totalDurationNanos must not be negative");
        }
        attempts = List.copyOf(attempts);
        if (resolutionPath == ResolutionPath.NOT_FOUND && document != null) {
            throw new IllegalArgumentException("document must be null when resolutionPath is NOT_FOUND");
        }
        if (resolutionPath != ResolutionPath.NOT_FOUND && document == null) {
            throw new IllegalArgumentException("document must be present for successful resolution paths");
        }
    }

    /**
     * Returns the resolved document when present.
     *
     * @return optional document
     */
    public Optional<StoredDocument> documentOptional() {
        return Optional.ofNullable(document);
    }

    /**
     * Indicates whether the request was resolved successfully.
     *
     * @return {@code true} when a document was found
     */
    public boolean found() {
        return document != null;
    }
}
