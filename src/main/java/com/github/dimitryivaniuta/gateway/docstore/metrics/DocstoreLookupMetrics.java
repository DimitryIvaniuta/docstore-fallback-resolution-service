package com.github.dimitryivaniuta.gateway.docstore.metrics;

import com.github.dimitryivaniuta.gateway.docstore.model.DocumentRequest;
import com.github.dimitryivaniuta.gateway.docstore.model.LookupAttempt;
import com.github.dimitryivaniuta.gateway.docstore.model.ResolutionPath;

/**
 * Observability port for Docstore resolution.
 *
 * <p>This interface is intentionally framework-neutral. A production integration can bridge it to
 * Micrometer, OpenTelemetry observations, or any internal metrics platform.</p>
 */
public interface DocstoreLookupMetrics {

    /**
     * Records one low-level lookup attempt.
     *
     * @param request original request
     * @param attempt attempt details
     */
    void recordAttempt(DocumentRequest request, LookupAttempt attempt);

    /**
     * Records the final resolution outcome.
     *
     * @param request original request
     * @param path final resolution path
     * @param totalDurationNanos end-to-end resolution duration
     */
    void recordResolution(DocumentRequest request, ResolutionPath path, long totalDurationNanos);

    /**
     * Records a terminal infrastructure failure.
     *
     * @param request original request
     * @param identifier identifier being attempted when the failure surfaced
     * @param totalDurationNanos elapsed time until failure
     * @param exception infrastructure failure
     */
    void recordInfrastructureFailure(
            DocumentRequest request,
            String identifier,
            long totalDurationNanos,
            RuntimeException exception);
}
