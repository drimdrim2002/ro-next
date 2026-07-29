package com.ronext.optimizer.adapter.in.http;

import java.util.List;

/**
 * Test seam around the current GCS calls. This is legacy-only and is not a
 * target storage port.
 */
interface LegacyStorage {
    /**
     * Opaque identity returned by one legacy listing operation.
     *
     * <p>The object key is diagnostic only. Implementations retain any
     * provider version/generation identity needed to read the exact object
     * observed by the listing.
     */
    interface CandidateObjectReference {
        String objectKey();
    }

    byte[] readResult(String bucket, String objectKey);

    void writeCandidate(String bucket, String objectKey, byte[] content);

    List<CandidateObjectReference> listCandidateObjects(String bucket, String prefix);

    byte[] readCandidate(String bucket, CandidateObjectReference reference);

    void writeResult(String bucket, String objectKey, byte[] content);
}
