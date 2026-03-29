package com.github.dimitryivaniuta.gateway.docstore.metrics;

import com.github.dimitryivaniuta.gateway.docstore.model.DocumentRequest;
import com.github.dimitryivaniuta.gateway.docstore.model.LookupAttempt;
import com.github.dimitryivaniuta.gateway.docstore.model.ResolutionPath;

/**
 * No-op metrics implementation suitable for tests or minimal embedding.
 */
public final class NoOpDocstoreLookupMetrics implements DocstoreLookupMetrics {

    /** Shared singleton instance. */
    public static final NoOpDocstoreLookupMetrics INSTANCE = new NoOpDocstoreLookupMetrics();

    private NoOpDocstoreLookupMetrics() {
    }

    @Override
    public void recordAttempt(DocumentRequest request, LookupAttempt attempt) {
        // no-op
    }

    @Override
    public void recordResolution(DocumentRequest request, ResolutionPath path, long totalDurationNanos) {
        // no-op
    }

    @Override
    public void recordInfrastructureFailure(
            DocumentRequest request,
            String identifier,
            long totalDurationNanos,
            RuntimeException exception) {
        // no-op
    }
}
