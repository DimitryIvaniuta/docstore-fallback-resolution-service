package com.github.dimitryivaniuta.gateway.docstore.service;

/**
 * Resolver safety switches intended for gradual rollout.
 *
 * @param legacyFallbackEnabled whether the resolver should attempt the historical composite key
 * @param throwWhenMissing whether {@code resolveRequired} should throw on a final miss
 */
public record DocstoreResolverProperties(
        boolean legacyFallbackEnabled,
        boolean throwWhenMissing) {

    /**
     * Returns the default safe rollout properties.
     *
     * @return defaults with fallback enabled and required resolution throwing on miss
     */
    public static DocstoreResolverProperties defaults() {
        return new DocstoreResolverProperties(true, true);
    }
}
