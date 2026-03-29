package com.github.dimitryivaniuta.gateway.docstore.service;

import com.github.dimitryivaniuta.gateway.docstore.client.DocstoreClient;
import com.github.dimitryivaniuta.gateway.docstore.client.DocstoreClientException;
import com.github.dimitryivaniuta.gateway.docstore.metrics.DocstoreLookupMetrics;
import com.github.dimitryivaniuta.gateway.docstore.metrics.NoOpDocstoreLookupMetrics;
import com.github.dimitryivaniuta.gateway.docstore.model.DocumentRequest;
import com.github.dimitryivaniuta.gateway.docstore.model.DocumentResolutionResult;
import com.github.dimitryivaniuta.gateway.docstore.model.LookupAttempt;
import com.github.dimitryivaniuta.gateway.docstore.model.ResolutionPath;
import com.github.dimitryivaniuta.gateway.docstore.model.StoredDocument;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Production-oriented implementation of the recommended Docstore workaround.
 *
 * <p>The service applies a strict sequence:</p>
 *
 * <ol>
 *   <li>Try the canonical product reference.</li>
 *   <li>If allowed and still not found, try the legacy composite identifier.</li>
 *   <li>Return a structured miss or throw a business exception, depending on the entry point.</li>
 *   <li>Never silently hide infrastructure failures behind a business miss.</li>
 * </ol>
 */
public class DocstoreFallbackResolverService {

    private final DocstoreClient docstoreClient;
    private final LegacyCompositeIdentifierBuilder identifierBuilder;
    private final DocstoreResolverProperties properties;
    private final DocstoreLookupMetrics metrics;

    /**
     * Creates the resolver using defaults and no-op metrics.
     *
     * @param docstoreClient low-level Docstore client
     * @param identifierBuilder legacy identifier builder
     */
    public DocstoreFallbackResolverService(
            DocstoreClient docstoreClient,
            LegacyCompositeIdentifierBuilder identifierBuilder) {
        this(docstoreClient, identifierBuilder, DocstoreResolverProperties.defaults(), NoOpDocstoreLookupMetrics.INSTANCE);
    }

    /**
     * Creates the resolver.
     *
     * @param docstoreClient low-level Docstore client
     * @param identifierBuilder legacy identifier builder
     * @param properties rollout and miss-handling properties
     * @param metrics observability port
     */
    public DocstoreFallbackResolverService(
            DocstoreClient docstoreClient,
            LegacyCompositeIdentifierBuilder identifierBuilder,
            DocstoreResolverProperties properties,
            DocstoreLookupMetrics metrics) {
        this.docstoreClient = Objects.requireNonNull(docstoreClient, "docstoreClient must not be null");
        this.identifierBuilder = Objects.requireNonNull(identifierBuilder, "identifierBuilder must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.metrics = Objects.requireNonNull(metrics, "metrics must not be null");
    }

    /**
     * Resolves a document and returns a structured result, including a clean NOT_FOUND outcome when
     * the business lookup misses under every configured identifier.
     *
     * @param request lookup request
     * @return structured resolution result
     * @throws DocumentResolutionInfrastructureException when a remote infrastructure failure occurs
     */
    public DocumentResolutionResult resolve(DocumentRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        long startedAt = System.nanoTime();
        List<LookupAttempt> attempts = new ArrayList<>(2);
        Set<String> identifiers = buildIdentifiers(request);

        for (String identifier : identifiers) {
            Optional<StoredDocument> document;
            long attemptStartedAt = System.nanoTime();
            try {
                document = docstoreClient.fetch(identifier, request.documentName());
            } catch (DocstoreClientException ex) {
                long attemptDuration = System.nanoTime() - attemptStartedAt;
                LookupAttempt attempt = LookupAttempt.failure(identifier, attemptDuration, ex.getMessage());
                attempts.add(attempt);
                metrics.recordAttempt(request, attempt);
                long totalDuration = System.nanoTime() - startedAt;
                metrics.recordInfrastructureFailure(request, identifier, totalDuration, ex);
                throw new DocumentResolutionInfrastructureException(
                        "Docstore lookup failed for identifier '%s'".formatted(identifier), ex);
            }

            long attemptDuration = System.nanoTime() - attemptStartedAt;
            if (document.isPresent()) {
                LookupAttempt attempt = LookupAttempt.hit(identifier, attemptDuration);
                attempts.add(attempt);
                metrics.recordAttempt(request, attempt);
                ResolutionPath path = identifier.equals(request.productReference())
                        ? ResolutionPath.CANONICAL
                        : ResolutionPath.LEGACY_COMPOSITE;
                long totalDuration = System.nanoTime() - startedAt;
                metrics.recordResolution(request, path, totalDuration);
                return new DocumentResolutionResult(document.get(), path, attempts, totalDuration);
            }

            LookupAttempt attempt = LookupAttempt.miss(identifier, attemptDuration);
            attempts.add(attempt);
            metrics.recordAttempt(request, attempt);
        }

        long totalDuration = System.nanoTime() - startedAt;
        metrics.recordResolution(request, ResolutionPath.NOT_FOUND, totalDuration);
        return new DocumentResolutionResult(null, ResolutionPath.NOT_FOUND, attempts, totalDuration);
    }

    /**
     * Resolves a document and throws a business exception when it is not found.
     *
     * @param request lookup request
     * @return successful resolution result
     * @throws DocumentNotFoundException when nothing was found and throwing is enabled
     * @throws DocumentResolutionInfrastructureException when a remote infrastructure failure occurs
     */
    public DocumentResolutionResult resolveRequired(DocumentRequest request) {
        DocumentResolutionResult result = resolve(request);
        if (!result.found() && properties.throwWhenMissing()) {
            throw new DocumentNotFoundException(buildNotFoundMessage(request, result));
        }
        return result;
    }

    private Set<String> buildIdentifiers(DocumentRequest request) {
        Set<String> identifiers = new LinkedHashSet<>(2);
        identifiers.add(request.productReference());
        if (properties.legacyFallbackEnabled()) {
            identifiers.add(identifierBuilder.build(
                    request.productReference(), request.format(), request.normalizedLanguage()));
        }
        return identifiers;
    }

    private String buildNotFoundMessage(DocumentRequest request, DocumentResolutionResult result) {
        String attemptedIdentifiers = result.attempts().stream()
                .map(LookupAttempt::identifier)
                .reduce((left, right) -> left + ", " + right)
                .orElse(request.productReference());
        return "Document '%s' was not found for product '%s'. Attempted identifiers: [%s]"
                .formatted(request.documentName(), request.productReference(), attemptedIdentifiers);
    }
}
