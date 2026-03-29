package com.github.dimitryivaniuta.gateway.docstore.service;

import com.github.dimitryivaniuta.gateway.docstore.model.DocumentFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Builds the historical composite Docstore identifier discussed in the meetings.
 *
 * <p>The pattern is:</p>
 *
 * <pre>{@code
 * productReference-FORMAT-language
 * }</pre>
 *
 * <p>The builder exists so the temporary consumer-side workaround can reproduce the legacy lookup
 * contract deterministically.</p>
 */
public final class LegacyCompositeIdentifierBuilder {

    /**
     * Creates the composite Docstore identifier.
     *
     * @param productReference canonical product/STOMP identifier
     * @param format document format segment
     * @param language document language segment
     * @return composite identifier compatible with the legacy convention
     */
    public String build(String productReference, DocumentFormat format, String language) {
        requireText(productReference, "productReference");
        Objects.requireNonNull(format, "format must not be null");
        requireText(language, "language");
        return productReference.trim() + '-' + format.name().toUpperCase(Locale.ROOT) + '-' + language.trim();
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
