package com.github.dimitryivaniuta.gateway.docstore.model;

import java.util.Objects;

/**
 * Diagnostic snapshot of a single lookup attempt.
 *
 * @param identifier identifier attempted against Docstore
 * @param outcome attempt result
 * @param durationNanos elapsed time for this attempt in nanoseconds
 * @param failureMessage infrastructure failure summary when {@code outcome == FAILURE}
 */
public record LookupAttempt(
        String identifier,
        AttemptOutcome outcome,
        long durationNanos,
        String failureMessage) {

    /**
     * Creates and validates the attempt record.
     */
    public LookupAttempt {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("identifier must not be blank");
        }
        Objects.requireNonNull(outcome, "outcome must not be null");
        if (durationNanos < 0) {
            throw new IllegalArgumentException("durationNanos must not be negative");
        }
        if (outcome != AttemptOutcome.FAILURE && failureMessage != null && !failureMessage.isBlank()) {
            throw new IllegalArgumentException("failureMessage is only allowed for FAILURE outcomes");
        }
        failureMessage = failureMessage == null || failureMessage.isBlank() ? null : failureMessage.trim();
    }

    /**
     * Creates a successful hit attempt.
     *
     * @param identifier attempted identifier
     * @param durationNanos elapsed time
     * @return attempt record
     */
    public static LookupAttempt hit(String identifier, long durationNanos) {
        return new LookupAttempt(identifier, AttemptOutcome.HIT, durationNanos, null);
    }

    /**
     * Creates a business miss attempt.
     *
     * @param identifier attempted identifier
     * @param durationNanos elapsed time
     * @return attempt record
     */
    public static LookupAttempt miss(String identifier, long durationNanos) {
        return new LookupAttempt(identifier, AttemptOutcome.MISS, durationNanos, null);
    }

    /**
     * Creates a failed attempt.
     *
     * @param identifier attempted identifier
     * @param durationNanos elapsed time
     * @param failureMessage infrastructure failure summary
     * @return attempt record
     */
    public static LookupAttempt failure(String identifier, long durationNanos, String failureMessage) {
        return new LookupAttempt(identifier, AttemptOutcome.FAILURE, durationNanos, failureMessage);
    }
}
