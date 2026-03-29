package com.github.dimitryivaniuta.gateway.docstore.model;

/**
 * Final resolution path for a successful lookup.
 */
public enum ResolutionPath {

    /** Document found under the canonical product/STOMP identifier. */
    CANONICAL,

    /** Document found only under the historical legacy composite identifier. */
    LEGACY_COMPOSITE,

    /** No document was found under any configured identifier. */
    NOT_FOUND
}
