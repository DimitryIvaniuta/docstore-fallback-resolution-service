package com.github.dimitryivaniuta.gateway.docstore.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable document payload returned from Docstore.
 *
 * @param resolvedIdentifier identifier under which the document was found
 * @param documentName logical document name or path
 * @param content binary payload
 * @param retrievedAt retrieval timestamp
 */
public record StoredDocument(
        String resolvedIdentifier,
        String documentName,
        byte[] content,
        Instant retrievedAt) {

    /**
     * Creates and validates the stored document.
     */
    public StoredDocument {
        requireText(resolvedIdentifier, "resolvedIdentifier");
        requireText(documentName, "documentName");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(retrievedAt, "retrievedAt must not be null");
        content = Arrays.copyOf(content, content.length);
    }

    /**
     * Returns a defensive copy of the content bytes.
     *
     * @return copied content bytes
     */
    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
