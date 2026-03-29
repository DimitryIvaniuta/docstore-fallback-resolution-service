package com.github.dimitryivaniuta.gateway.docstore.service;

import com.github.dimitryivaniuta.gateway.docstore.model.DocumentRequest;
import com.github.dimitryivaniuta.gateway.docstore.model.DocumentResolutionResult;
import java.util.Objects;

/**
 * Consumer-facing facade that hides Docstore identifier complexity from the rest of the application.
 *
 * <p>In a real service this class would sit behind an application service or controller boundary and
 * translate infrastructure exceptions into service-level error responses.</p>
 */
public class SppDocsDocumentFacade {

    private final DocstoreFallbackResolverService fallbackResolverService;

    /**
     * Creates the facade.
     *
     * @param fallbackResolverService resolver implementing canonical-first lookup
     */
    public SppDocsDocumentFacade(DocstoreFallbackResolverService fallbackResolverService) {
        this.fallbackResolverService = Objects.requireNonNull(
                fallbackResolverService, "fallbackResolverService must not be null");
    }

    /**
     * Retrieves a document using the configured fallback strategy.
     *
     * @param request request describing the desired document
     * @return structured resolution result
     */
    public DocumentResolutionResult getDocument(DocumentRequest request) {
        return fallbackResolverService.resolveRequired(request);
    }
}
