package com.github.dimitryivaniuta.gateway.docstore.model;

/**
 * Outcome of one Docstore lookup attempt.
 */
public enum AttemptOutcome {

    /** A document was found. */
    HIT,

    /** No document was found under the attempted identifier. */
    MISS,

    /** The remote call failed due to infrastructure or contract issues. */
    FAILURE
}
