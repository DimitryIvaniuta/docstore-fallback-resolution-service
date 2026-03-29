package com.github.dimitryivaniuta.gateway.docstore.model;

import java.util.Locale;
import java.util.Objects;

/**
 * Immutable request model for Docstore lookup.
 *
 * <p>The request carries both the canonical lookup key and the attributes required to reproduce the
 * historical composite identifier. Validation is done eagerly so invalid requests fail before any
 * remote call is attempted.</p>
 *
 * @param productReference canonical product/STOMP identifier
 * @param documentName logical document name or path segment
 * @param format document format used in the legacy composite key
 * @param language document language used in the legacy composite key
 * @param correlationId caller-provided correlation id for logs/traces/metrics
 */
public record DocumentRequest(
        String productReference,
        String documentName,
        DocumentFormat format,
        String language,
        String correlationId) {

    /**
     * Creates and validates the request.
     */
    public DocumentRequest {
        requireText(productReference, "productReference");
        requireText(documentName, "documentName");
        Objects.requireNonNull(format, "format must not be null");
        requireText(language, "language");
        correlationId = normalizeOptional(correlationId);
        language = language.trim();
        documentName = documentName.trim();
        productReference = productReference.trim();
    }

    /**
     * Convenience constructor for callers without a correlation id.
     *
     * @param productReference canonical product/STOMP identifier
     * @param documentName logical document name or path segment
     * @param format document format used in the legacy composite key
     * @param language document language used in the legacy composite key
     */
    public DocumentRequest(String productReference, String documentName, DocumentFormat format, String language) {
        this(productReference, documentName, format, language, null);
    }

    /**
     * Returns a normalized language token suitable for legacy key construction.
     *
     * @return trimmed language token with original case preserved
     */
    public String normalizedLanguage() {
        return language.trim();
    }

    /**
     * Returns a low-cardinality format token suitable for metrics tags.
     *
     * @return lowercase format token
     */
    public String formatTag() {
        return format.name().toLowerCase(Locale.ROOT);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
