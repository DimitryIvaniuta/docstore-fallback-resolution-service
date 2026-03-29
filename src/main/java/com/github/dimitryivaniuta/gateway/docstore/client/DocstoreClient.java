package com.github.dimitryivaniuta.gateway.docstore.client;

import com.github.dimitryivaniuta.gateway.docstore.model.StoredDocument;
import java.util.Optional;

/**
 * Low-level Docstore lookup contract used by the fallback resolver.
 *
 * <p>The interface is intentionally narrow: the recommended resolution strategy changes the consumer
 * lookup flow, not the Docstore storage model. Implementations can call HTTP, gRPC, or a vendor SDK
 * behind this abstraction.</p>
 */
public interface DocstoreClient {

    /**
     * Retrieves a document by Docstore identifier and logical document name.
     *
     * <p>Returning {@link Optional#empty()} is reserved for a normal business miss. Transport or
     * platform failures should throw a {@link DocstoreClientException} subtype so callers can
     * distinguish "not found" from infrastructure failure.</p>
     *
     * @param identifier canonical or legacy Docstore identifier
     * @param documentName logical document name or path segment
     * @return document payload when present, otherwise an empty optional
     * @throws DocstoreClientException when the Docstore call fails at the infrastructure layer
     */
    Optional<StoredDocument> fetch(String identifier, String documentName);
}
