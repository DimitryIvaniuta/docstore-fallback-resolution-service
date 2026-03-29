# Docstore fallback resolution - production-grade Java approach

This project implements the **recommended short-term Docstore fix** from the meeting analysis:

1. **Do not immediately change producer upload logic** in Document Service.
2. **Fix retrieval on the consumer side first**.
3. Query Docstore by the **canonical product key** first.
4. If not found, query again using the **legacy composite key** (`productRef-FORMAT-language`).
5. Measure canonical hits, legacy fallback hits, misses, and infrastructure failures.
6. Keep producer-side redesign for a later controlled rollout.

## What was improved vs the initial version

The project was hardened into a more production-grade design:

- validated immutable request/result models,
- explicit **resolver properties** for safe rollout,
- optional **non-throwing** and **throwing** resolution APIs,
- structured **attempt diagnostics** with duration and failure reason,
- **metrics port** to plug Micrometer/Prometheus later,
- **retry decorator** for transient Docstore client failures,
- explicit **infrastructure exception taxonomy**,
- deduplication so the same identifier is never queried twice,
- consumer-facing facade that keeps Docstore complexity out of the rest of the app,
- wider test coverage for fallback, misses, disabled fallback, and transient failures.

## Recommended rollout path

### Step 1 - Apply the consumer-side fallback
Use `SppDocsDocumentFacade` or wire `DocstoreFallbackResolverService` directly into the consumer that currently queries Docstore.

The lookup sequence is:

```text
canonical product key -> legacy composite key -> miss
```

### Step 2 - Add observability
Hook the provided `DocstoreLookupMetrics` port into your metrics implementation.
Recommended metrics:

- canonical hit count,
- legacy fallback hit count,
- miss count,
- infrastructure failure count,
- end-to-end resolution latency.

This aligns with Micrometer guidance that timed operations already include a count, so timing the resolution call is preferred over adding duplicate counters for the same timed event. Micrometer also recommends lowercase dot notation for meter names. citeturn853136search0turn853136search2turn853136search4

### Step 3 - Add resilience where the real HTTP client lives
Wrap the real HTTP Docstore client with the provided `RetryingDocstoreClientDecorator`.
Use it **only** for transient infrastructure failures, not for a normal `not found` result.

This matches resilience4j guidance that retries are best applied around transient failures and can be combined with additional decorators later if needed. citeturn853136search3turn853136search9

### Step 4 - Keep producer upload logic unchanged for now
Do **not** switch to dual uploads yet. The meeting analysis showed that upload traffic and Docstore latency are already a concern. This project intentionally fixes the visible consumer problem first.

### Step 5 - Later producer-side cleanup
Once hidden consumers are mapped and performance is stabilized:

- store all documents under the canonical product/STOMP identifier,
- treat format/language as rendition/version concerns,
- roll the producer change out behind a flag.

## Main classes

- `DocstoreClient` - minimal Docstore lookup contract.
- `RetryingDocstoreClientDecorator` - retries transient infrastructure errors.
- `DocstoreFallbackResolverService` - canonical-first fallback resolver.
- `LegacyCompositeIdentifierBuilder` - reproduces the historical composite key.
- `DocstoreResolverProperties` - rollout and safety switches.
- `DocstoreLookupMetrics` - observability port.
- `SppDocsDocumentFacade` - example consumer entry point.

## Build

```bash
./gradlew test
```
