package com.github.dimitryivaniuta.gateway.docstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.dimitryivaniuta.gateway.docstore.client.DocstoreClient;
import com.github.dimitryivaniuta.gateway.docstore.client.TransientDocstoreClientException;
import com.github.dimitryivaniuta.gateway.docstore.metrics.DocstoreLookupMetrics;
import com.github.dimitryivaniuta.gateway.docstore.model.AttemptOutcome;
import com.github.dimitryivaniuta.gateway.docstore.model.DocumentFormat;
import com.github.dimitryivaniuta.gateway.docstore.model.DocumentRequest;
import com.github.dimitryivaniuta.gateway.docstore.model.DocumentResolutionResult;
import com.github.dimitryivaniuta.gateway.docstore.model.LookupAttempt;
import com.github.dimitryivaniuta.gateway.docstore.model.ResolutionPath;
import com.github.dimitryivaniuta.gateway.docstore.model.StoredDocument;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DocstoreFallbackResolverService}.
 */
class DocstoreFallbackResolverServiceTest {

    private InMemoryDocstoreClient docstoreClient;
    private RecordingMetrics metrics;
    private DocstoreFallbackResolverService service;

    @BeforeEach
    void setUp() {
        docstoreClient = new InMemoryDocstoreClient();
        metrics = new RecordingMetrics();
        service = new DocstoreFallbackResolverService(
                docstoreClient,
                new LegacyCompositeIdentifierBuilder(),
                DocstoreResolverProperties.defaults(),
                metrics);
    }

    @Test
    void shouldReturnCanonicalDocumentWhenPresent() {
        docstoreClient.put("123456P", "client-term-sheet-final", document("123456P", "client-term-sheet-final"));

        DocumentResolutionResult result = service.resolveRequired(new DocumentRequest(
                "123456P", "client-term-sheet-final", DocumentFormat.PDF, "English", "corr-1"));

        assertThat(result.resolutionPath()).isEqualTo(ResolutionPath.CANONICAL);
        assertThat(result.document().resolvedIdentifier()).isEqualTo("123456P");
        assertThat(result.attempts()).hasSize(1);
        assertThat(metrics.finalPaths).containsExactly(ResolutionPath.CANONICAL);
    }

    @Test
    void shouldFallBackToLegacyCompositeIdentifier() {
        docstoreClient.put(
                "123456P-PDF-English",
                "client-term-sheet-final",
                document("123456P-PDF-English", "client-term-sheet-final"));

        DocumentResolutionResult result = service.resolveRequired(new DocumentRequest(
                "123456P", "client-term-sheet-final", DocumentFormat.PDF, "English"));

        assertThat(result.resolutionPath()).isEqualTo(ResolutionPath.LEGACY_COMPOSITE);
        assertThat(result.document().resolvedIdentifier()).isEqualTo("123456P-PDF-English");
        assertThat(result.attempts()).hasSize(2);
        assertThat(result.attempts().get(0).outcome()).isEqualTo(AttemptOutcome.MISS);
        assertThat(result.attempts().get(1).outcome()).isEqualTo(AttemptOutcome.HIT);
        assertThat(metrics.finalPaths).containsExactly(ResolutionPath.LEGACY_COMPOSITE);
    }

    @Test
    void shouldReturnStructuredMissWhenUsingNonThrowingApi() {
        DocumentResolutionResult result = service.resolve(new DocumentRequest(
                "123456P", "client-term-sheet-final", DocumentFormat.PDF, "English"));

        assertThat(result.found()).isFalse();
        assertThat(result.resolutionPath()).isEqualTo(ResolutionPath.NOT_FOUND);
        assertThat(result.attempts()).hasSize(2);
        assertThat(metrics.finalPaths).containsExactly(ResolutionPath.NOT_FOUND);
    }

    @Test
    void shouldThrowWhenDocumentCannotBeFoundUnderEitherIdentifier() {
        DocumentRequest request = new DocumentRequest("123456P", "client-term-sheet-final", DocumentFormat.PDF, "English");

        assertThatThrownBy(() -> service.resolveRequired(request))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessageContaining("123456P")
                .hasMessageContaining("123456P-PDF-English");
    }

    @Test
    void shouldSkipLegacyLookupWhenFallbackDisabled() {
        DocstoreFallbackResolverService fallbackDisabledService = new DocstoreFallbackResolverService(
                docstoreClient,
                new LegacyCompositeIdentifierBuilder(),
                new DocstoreResolverProperties(false, true),
                metrics);

        assertThatThrownBy(() -> fallbackDisabledService.resolveRequired(
                new DocumentRequest("123456P", "client-term-sheet-final", DocumentFormat.PDF, "English")))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessageContaining("123456P")
                .hasMessageNotContaining("123456P-PDF-English");
    }

    @Test
    void shouldFailFastOnInfrastructureErrorInsteadOfPretendingItIsAMiss() {
        docstoreClient.failIdentifier("123456P", new TransientDocstoreClientException("timeout"));

        assertThatThrownBy(() -> service.resolveRequired(
                new DocumentRequest("123456P", "client-term-sheet-final", DocumentFormat.PDF, "English")))
                .isInstanceOf(DocumentResolutionInfrastructureException.class)
                .hasMessageContaining("123456P")
                .hasRootCauseInstanceOf(TransientDocstoreClientException.class);

        assertThat(metrics.infrastructureFailures).hasSize(1);
    }

    private StoredDocument document(String identifier, String documentName) {
        return new StoredDocument(identifier, documentName, "ok".getBytes(), Instant.parse("2026-03-28T10:15:30Z"));
    }

    /**
     * Tiny in-memory test double used by the unit tests.
     */
    private static final class InMemoryDocstoreClient implements DocstoreClient {
        private final Map<String, StoredDocument> storage = new HashMap<>();
        private final Map<String, RuntimeException> failures = new HashMap<>();

        void put(String identifier, String documentName, StoredDocument document) {
            storage.put(key(identifier, documentName), document);
        }

        void failIdentifier(String identifier, RuntimeException exception) {
            failures.put(identifier, exception);
        }

        @Override
        public Optional<StoredDocument> fetch(String identifier, String documentName) {
            if (failures.containsKey(identifier)) {
                throw failures.get(identifier);
            }
            return Optional.ofNullable(storage.get(key(identifier, documentName)));
        }

        private String key(String identifier, String documentName) {
            return identifier + "::" + documentName;
        }
    }

    /**
     * Recording metrics test double.
     */
    private static final class RecordingMetrics implements DocstoreLookupMetrics {
        private final List<LookupAttempt> attempts = new ArrayList<>();
        private final List<ResolutionPath> finalPaths = new ArrayList<>();
        private final List<String> infrastructureFailures = new ArrayList<>();

        @Override
        public void recordAttempt(DocumentRequest request, LookupAttempt attempt) {
            attempts.add(attempt);
        }

        @Override
        public void recordResolution(DocumentRequest request, ResolutionPath path, long totalDurationNanos) {
            finalPaths.add(path);
        }

        @Override
        public void recordInfrastructureFailure(
                DocumentRequest request,
                String identifier,
                long totalDurationNanos,
                RuntimeException exception) {
            infrastructureFailures.add(identifier + ":" + exception.getMessage());
        }
    }
}
